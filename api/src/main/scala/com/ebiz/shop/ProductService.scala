package com.ebiz.shop

import cats.Applicative
import cats.implicits._
import com.ebiz.shop.ProductService.domain._

import java.util.UUID
import scala.collection.mutable

sealed trait ProductService[F[_]] {
  def get(id: String): F[Option[ShopProduct]]

  def getAll(): F[Seq[ShopProduct]]

  def add(product: ProductCreationRequest): F[Either[ErrorMsg, ShopProduct]]

  def update(id: String, update: ProductUpdateRequest): F[UpdateStatus]

  def delete(id: String): F[DeleteStatus]
}

object ProductService {

  object domain {

    case class ShopProduct(id: String, description: String, name: String, category: String)

    type ErrorMsg = String

    sealed trait UpdateStatus

    final case class UpdateSuccess(id: String) extends UpdateStatus

    final case class UpdateFailure(id: String, msg: ErrorMsg) extends UpdateStatus

    sealed trait DeleteStatus

    final case class DeleteSuccess(id: String) extends DeleteStatus

    final case class DeleteFailure(id: String, msg: ErrorMsg) extends DeleteStatus

  }

  object infra {
    def impl[F[_] : Applicative]: ProductService[F] = new ProductService[F] {

      private val db: mutable.Map[String, ShopProduct] = mutable.HashMap.empty[String, ShopProduct]

      def get(id: String): F[Option[ShopProduct]] = db.get(id).pure[F]

      def getAll(): F[Seq[ShopProduct]] = db.values.toSeq.pure[F]

      def add(product: ProductCreationRequest): F[Either[ErrorMsg, ShopProduct]] =
        UUID.randomUUID().toString.pure[F]
          .map { uuid =>
            db.put(uuid, ShopProduct(uuid, product.description, product.name, product.category)) match {
              case None => db.get(uuid) match {
                case None => Left(s"Product not added, description: ${product.name}")
                case Some(product) => Right(product)
              }
              case Some(taproductk) => Right(taproductk)
            }
          }

      def update(id: String, update: ProductUpdateRequest): F[UpdateStatus] = {
        db.get(id) match {
          case None => onUpdateOnAbsent(id)
          case Some(_) => onStatusChangedOnPresent(id, update)
        }
      }.pure[F]

      def delete(id: String): F[DeleteStatus] = onDelete(id).pure[F]

      private def onDelete(id: String): DeleteStatus = db.remove(id) match {
        case None => DeleteFailure(id, "Product not deleted")
        case Some(_) => DeleteSuccess(id)
      }

      private def onUpdateOnAbsent(id: String): UpdateStatus =
        UpdateFailure(id, s"Product with id: $id not found")

      private def onStatusChangedOnPresent(id: String, update: ProductUpdateRequest): UpdateStatus =
        db.put(id, ShopProduct(id, update.description, update.name, update.category)) match {
          case None => UpdateFailure(id, "Couldn't update product")
          case Some(product) => UpdateSuccess(product.id)
        }

    }
  }

}