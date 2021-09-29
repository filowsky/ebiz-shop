package com.ebiz.shop

import cats.effect.{ConcurrentEffect, Sync}
import cats.implicits._
import com.ebiz.shop.ProductService.domain._
import com.ebiz.shop.encoder._
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.webtoken.JsonWebSignature
import com.google.auth.oauth2.TokenVerifier
import io.circe.Json
import io.circe.generic.auto._
import io.circe.parser.parse
import org.http4s._
import org.http4s.circe._
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Location
import org.http4s.implicits.http4sLiteralsSyntax
import org.http4s.util.CaseInsensitiveString

import scala.concurrent.ExecutionContext.global
import scala.util.{Failure, Success, Try}

object ShopRoutes {


  private val verifier: TokenVerifier = TokenVerifier.newBuilder().setHttpTransportFactory(() => new NetHttpTransport()).build()

  def productsRoutes[F[_] : Sync, T[_]](P: ProductService[F], U: UsersService[F])(implicit cf: ConcurrentEffect[F]): HttpRoutes[F] = {

    def extractAndCheckTokenData(jwt: JsonWebSignature, raw: String): Boolean = {
      val payload = jwt.getPayload
      val userId = payload.getSubject
      val userName = payload.get("name").asInstanceOf[String]
      val emailVerified = payload.get("email_verified").asInstanceOf[Boolean]
      if (emailVerified) {
        U.save(User(userId, userName, true, raw))
        true
      } else {
        false
      }
    }

    def verifyWithPresentData(token: String): F[Boolean] = {
      Try {
        verifier.verify(token)
      } match {
        case Failure(_) => if (token != "undefined") invalidateToken(token) else false.pure[F]
        case Success(value) => U.validateToken(value.getPayload.getSubject)
      }
    }

    def invalidateToken(token: String): F[Boolean] = {
      import java.util.Base64
      val chunks = token.split("\\.")
      val decoder = Base64.getDecoder
      val payload = new String(decoder.decode(chunks(1)))
      parse(payload) match {
        case Right(value) =>
          val raw = value.\\("sub").head.toString()
          val id_1 = raw.substring(1)
          val id = id_1.substring(0, id_1.length - 1)
          U.updateValidation(id, isValid = false)
        case Left(_) => false.pure[F]
      }
    }

    def checkAndProcessTokenData(token: String): Boolean = {
      if (token == null) false
      else {
        Try {
          verifier.verify(token)
        } match {
          case Success(jwt) => extractAndCheckTokenData(jwt, token)
          case Failure(_) => false
        }
      }
    }

    def authorize(request: Request[F]): F[Boolean] = request.headers.get(CaseInsensitiveString.apply("Authorization")) match {
      case None =>
        request.headers.get(CaseInsensitiveString.apply("User-Id")) match {
          case None => false.pure[F]
          case Some(id) => U.get(id.value).flatMap {
            case None => false.pure[F]
            case Some(value) => verifyWithPresentData(value.token)
          }
        }
      case Some(token) => verifyWithPresentData(token.value)
    }

    implicit val clientResource = BlazeClientBuilder[F](global).resource

    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {

      case req@POST -> Root / "auth" =>
        for {
          authReq <- req.as[String]
          authCookie = authReq.split("&")(0).split("=")(1)
          verified = checkAndProcessTokenData(authCookie)
          resp <- {
            if (verified) NotModified(Location(uri"https://ebiz-shop-frontend-brqleqljrq-lm.a.run.app/products"))
            else NotFound(errorBody(s"Couldn't authorize."))
          }
        } yield {
          if (verified) resp.addCookie(ResponseCookie(name = "shop_auth", content = authCookie))
          else resp
        }

      case req@GET -> Root / "products" => for {
        auth <- authorize(req)
        resp <- if (auth) {
          for {
            products <- P.getAll()
            resp <- products match {
              case Nil => NotFound(errorBody(s"No products"))
              case _ => Ok(products)
            }
          } yield resp
        } else {
          Forbidden()
        }
      } yield resp

      case req@GET -> Root / "products" / id => for {
        auth <- authorize(req)
        resp <- if (auth) {
          for {
            product <- P.get(id)
            resp <- product match {
              case None => NotFound(errorBody(s"Product $id not found"))
              case Some(product) => Ok(product)
            }
          } yield resp
        } else {
          Forbidden()
        }
      } yield resp
      //
      case req@POST -> Root / "products" =>
        implicit val decoder: EntityDecoder[F, ProductCreationRequest] = jsonOf[F, ProductCreationRequest]
        for {
          auth <- authorize(req)
          resp <- if (auth) {
            for {
              productCreationRequest <- req.as[ProductCreationRequest]
              addResponse <- P.add(productCreationRequest)
              resp <- addResponse match {
                case Right(value) => Ok(value)
                case Left(msg) => BadRequest(errorBody(msg))
              }
            } yield resp
          } else {
            Forbidden()
          }
        } yield resp

      case req@PUT -> Root / "products" / id =>
        implicit val statusDecoder: EntityDecoder[F, ProductUpdateRequest] = jsonOf[F, ProductUpdateRequest]
        for {
          auth <- authorize(req)
          resp <- if (auth) {
            for {
              productUpdateRequest <- req.as[ProductUpdateRequest]
              addResponse <- P.update(id, productUpdateRequest)
              resp <- Ok(addResponse)
            } yield resp
          } else {
            Forbidden()
          }
        } yield resp

      case req@DELETE -> Root / "products" / id =>
        for {
          auth <- authorize(req)
          resp <- if (auth) {
            for {
              deleteStatus <- P.delete(id)
              resp <- Ok(deleteStatus)
            } yield resp
          } else {
            Forbidden()
          }
        } yield resp
    }
  }

  private def errorBody(message: ErrorMsg): Json = Json.obj(
    ("message", Json.fromString(message))
  )
}

final case class ProductCreationRequest(name: String, description: String, category: String)

final case class ProductUpdateRequest(name: String, description: String, category: String)