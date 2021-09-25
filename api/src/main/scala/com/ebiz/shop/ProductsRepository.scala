package com.ebiz.shop

import com.ebiz.shop.ProductService.domain._
import slick.jdbc.SQLiteProfile.api._

import java.util.UUID
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

sealed trait ProductsRepository[F[_]] {
  def get(id: String): F[Option[ShopProduct]]

  def getAll: F[Seq[ShopProduct]]

  def add(product: ProductCreationRequest): F[Either[ErrorMsg, ShopProduct]]

  def update(id: String, update: ProductUpdateRequest): F[UpdateStatus]

  def delete(id: String): F[DeleteStatus]
}


class SlickProductsRepository(db: Database) extends ProductsRepository[Future] {

  import scala.concurrent.ExecutionContext.Implicits.global

  val products = TableQuery[ProductTable]

  db.run(DBIO.seq(products.schema.create))

  override def get(id: String): Future[Option[ShopProduct]] = {
    db.run {
      products.filter(e => e.id === id).result.headOption
    }.map { x =>
      x.map(entity => ShopProduct(entity._1, entity._2, entity._3, entity._4))
    }
  }

  override def getAll(): Future[Seq[ShopProduct]] = {
    db.run {
      products.result
    }.map { x =>
      x.map(e => {
        ShopProduct(e._1, e._2, e._3, e._4)
      })
    }
  }

  override def add(product: ProductCreationRequest): Future[Either[ErrorMsg, ShopProduct]] = {
    val id = UUID.randomUUID().toString
    db.run {
      DBIO.seq(
        products += (id, product.name, product.description, product.category)
      )
    }.transform {
      case Success(_) => Try(ShopProduct(id, product.name, product.description, product.category))
      case Failure(_) => Try(null)
    }.map {
      case null => Left("Unable to add product")
      case value => Right(value)
    }
  }

  override def delete(id: String): Future[DeleteStatus] = {
    db.run {
      products.filter(_.id === id).delete
    }.map[DeleteStatus] {
      case 0 => DeleteFailure(id, s"Unable to delete ${id}")
      case _ => DeleteSuccess(id)
    }
  }

  def update(id: String, update: ProductUpdateRequest): Future[UpdateStatus] = {
    db.run {
      products.filter(_.id === id).update(id, update.name, update.description, update.category)
    }.map {
      case 0 => UpdateFailure(id, s"Unable to update ${id}")
      case _ => UpdateSuccess(id)
    }
  }
}


class ProductTable(tag: Tag) extends Table[(String, String, String, String)](tag, "products") {
  def id = column[String]("id", O.PrimaryKey)

  def name = column[String]("name")

  def description = column[String]("description")

  def category = column[String]("category")

  def * = (id, name, description, category)
}
