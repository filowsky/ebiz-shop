package com.ebiz.shop

import cats.Applicative
import cats.implicits._
import com.ebiz.shop.ProductService.domain._

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
    def synchronousProductService[F[_] : Applicative](productsRepository: SlickProductsRepository): ProductService[F] = new ProductService[F] {

      def get(id: String): F[Option[ShopProduct]] = utils.await(productsRepository.get(id)).pure[F]

      def getAll(): F[Seq[ShopProduct]] = utils.await(productsRepository.getAll()).pure[F]

      def add(product: ProductCreationRequest): F[Either[ErrorMsg, ShopProduct]] = utils.await(productsRepository.add(product)).pure[F]

      def update(id: String, update: ProductUpdateRequest): F[UpdateStatus] = utils.await(productsRepository.update(id, update)).pure[F]

      def delete(id: String): F[DeleteStatus] = utils.await(productsRepository.delete(id)).pure[F]
    }
  }

}