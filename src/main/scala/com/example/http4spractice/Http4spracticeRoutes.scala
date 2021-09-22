package com.example.http4spractice

import cats.Applicative
import cats.effect.{ConcurrentEffect, Sync}
import cats.implicits._
import com.example.http4spractice.ProductService.domain._
import com.example.http4spractice.encoder._
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.auth.http.HttpTransportFactory
import com.google.auth.oauth2.{AccessToken, GoogleCredentials, TokenVerifier}
import io.circe.generic.auto._
import io.circe.{Encoder, Json}
import org.http4s.circe._
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes}

import java.util.Date
import scala.concurrent.ExecutionContext.global

object Http4spracticeRoutes {

  def productsRoutes[F[_] : Sync](P: ProductService[F])(implicit cf: ConcurrentEffect[F]): HttpRoutes[F] = {
    implicit val clientResource = BlazeClientBuilder[F](global).resource

    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {

      case req@POST -> Root / "auth" =>
        for {
          authReq <- req.as[String]
          resp <- authReq match {
            case null => NotFound(errorBody(s"Empty body."))
            case x =>
              val h = req.headers
              val coo = req.cookies
              val att = req.attributes
              val token = x.split("&")(0).split("=")(1)
              val csrf = x.split("&")(1).split("=")(1)
              println(s"Token: $token, CSRF: $csrf")
              val creds = GoogleCredentials.create(
                new AccessToken(
                  token,
                  Date.from(java.time.ZonedDateTime.now.plusDays(2).toInstant)
                )
              )
              println(s"Access token: $creds")
              val verifier = TokenVerifier.newBuilder().setHttpTransportFactory(new HttpTransportFactory {
                override def create(): HttpTransport = new NetHttpTransport()
              }).build()
              val result = verifier.verify(token)
              //credential = jwt token
              //result = we can have client_id~aud from there and exp time for this token (1 hour)
              //token can be held in cookie
              //              clientResource.use { c =>
              //                val link = uri"http://localhost:3000"
              //                val req: Request[F] = Request(GET, link)
              //                val redirectClient = Logger(true, true, _ => false)(FollowRedirect[F](10, _ => true)(c))
              //                redirectClient.toHttpApp.run(req)
              //              }
              Ok()
          }
        } yield resp
      //        clientResource.use { c =>
      //          val link = uri"http://localhost:3000"
      //          val req: Request[F] = Request(GET, link)
      //          val redirectClient = Logger(true, true, _ => false)(FollowRedirect[F](10, _ => true)(c))
      //          redirectClient.toHttpApp.run(req)
      //        }

      case GET -> Root / "products" =>
        for {
          products <- P.getAll()
          resp <- products match {
            case Nil => NotFound(errorBody(s"No products"))
            case _ => Ok(products)
          }
        } yield resp

      case GET -> Root / "products" / id =>
        for {
          product <- P.get(id)
          resp <- product match {
            case None => NotFound(errorBody(s"Product $id not found"))
            case Some(product) => Ok(product)
          }
        } yield resp

      case req@POST -> Root / "products" =>
        implicit val decoder: EntityDecoder[F, ProductCreationRequest] = jsonOf[F, ProductCreationRequest]
        for {
          productCreationRequest <- req.as[ProductCreationRequest]
          addResponse <- P.add(productCreationRequest)
          resp <- addResponse match {
            case Right(value) => Ok(value)
            case Left(msg) => BadRequest(errorBody(msg))
          }
        } yield resp

      case req@PUT -> Root / "products" / id =>
        implicit val statusDecoder: EntityDecoder[F, ProductUpdateRequest] = jsonOf[F, ProductUpdateRequest]
        for {
          productUpdateRequest <- req.as[ProductUpdateRequest]
          addResponse <- P.update(id, productUpdateRequest)
          resp <- Ok(addResponse)
        } yield resp


      case DELETE -> Root / "products" / id =>
        for {
          deleteResponse <- P.delete(id)
          resp <- Ok(deleteResponse)
        } yield resp
    }
  }

  private def errorBody(message: ErrorMsg): Json = Json.obj(
    ("message", Json.fromString(message))
  )
}

object encoder {
  implicit val productEncoder: Encoder[ShopProduct] = new Encoder[ShopProduct] {
    final def apply(product: ShopProduct): Json =
      Json.obj(
        ("id", Json.fromString(product.id)),
        ("name", Json.fromString(product.name)),
        ("description", Json.fromString(product.description)),
        ("category", Json.fromString(product.category)),
      )
  }

  implicit def productEntityEncoder[F[_] : Applicative]: EntityEncoder[F, ShopProduct] =
    jsonEncoderOf[F, ShopProduct]

  implicit val productsEncoder: Encoder[Seq[ShopProduct]] = new Encoder[Seq[ShopProduct]] {
    override def apply(products: Seq[ShopProduct]): Json = Json.arr(
      products.map { product =>
        Json.obj(
          ("id", Json.fromString(product.id)),
          ("description", Json.fromString(product.description)),
          ("name", Json.fromString(product.name)),
          ("category", Json.fromString(product.category))
        )
      }: _*
    )
  }

  implicit def productsEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Seq[ShopProduct]] =
    jsonEncoderOf[F, Seq[ShopProduct]]

  implicit val updateStatusEncoder: Encoder[UpdateStatus] = new Encoder[UpdateStatus] {
    final def apply(updateStatus: UpdateStatus): Json = updateStatus match {
      case UpdateFailure(id, msg) =>
        Json.obj(
          ("id", Json.fromString(id)),
          ("msg", Json.fromString(msg))
        )
      case UpdateSuccess(id) =>
        Json.obj(
          ("id", Json.fromString(id))
        )
    }
  }

  implicit def updateStatusEntityEncoder[F[_] : Applicative]: EntityEncoder[F, UpdateStatus] =
    jsonEncoderOf[F, UpdateStatus]

  implicit val deleteStatusEncoder: Encoder[DeleteStatus] = new Encoder[DeleteStatus] {
    override def apply(a: DeleteStatus): Json = a match {
      case DeleteFailure(id, msg) =>
        Json.obj(
          ("id", Json.fromString(id)),
          ("msg", Json.fromString(msg))
        )
      case DeleteSuccess(id) =>
        Json.obj(
          ("id", Json.fromString(id))
        )
    }
  }

  implicit def deleteEntityEncoder[F[_] : Applicative]: EntityEncoder[F, DeleteStatus] =
    jsonEncoderOf[F, DeleteStatus]

}

final case class ProductCreationRequest(name: String, description: String, category: String)

final case class ProductUpdateRequest(name: String, description: String, category: String)