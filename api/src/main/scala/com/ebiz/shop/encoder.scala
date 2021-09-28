package com.ebiz.shop

import cats.Applicative
import com.ebiz.shop.ProductService.domain.{DeleteFailure, DeleteStatus, DeleteSuccess, ShopProduct, UpdateFailure, UpdateStatus, UpdateSuccess}
import io.circe.{Encoder, Json}
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf

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