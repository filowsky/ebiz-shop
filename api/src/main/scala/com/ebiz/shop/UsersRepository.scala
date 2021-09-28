package com.ebiz.shop

import com.ebiz.shop.ProductService.domain.ErrorMsg
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

sealed trait UsersRepository[F[_]] {
  def save(user: User): F[Either[ErrorMsg, Unit]]

  def get(id: String): F[Option[User]]

  def validateToken(id: String): F[Boolean]

  def updateValidation(id: String, isValid: Boolean): F[Boolean]

  def isUserPresent(id: String): F[Boolean]
}

class SlickUsersRepository(db: Database) extends UsersRepository[Future] {

  import scala.concurrent.ExecutionContext.Implicits.global

  val users = TableQuery[UserTable]

  db.run(DBIO.seq(users.schema.create))

  override def save(user: User): Future[Either[ErrorMsg, Unit]] = {
    db.run {
      DBIO.seq(
        users += (user.id, user.name, user.validToken)
      )
    }.transform {
      case Success(_) => Try(Unit)
      case Failure(_) => Try(null)
    }.map {
      case null => Left("Unable to add product")
      case _ => Right(Unit)
    }
  }

  override def get(id: String): Future[Option[User]] = db.run {
    users.filter(u => u.id === id).result.headOption
  }.map { x =>
    x.map(entity => User(entity._1, entity._2, entity._3))
  }

  override def validateToken(id: String): Future[Boolean] = get(id).map {
    case None => false
    case Some(user) => user.validToken
  }

  override def updateValidation(id: String, isValid: Boolean): Future[Boolean] = db.run {
    users.filter(_.id === id).map(_.validToken).update(isValid).map {
      case 0 => false
      case _ => true
    }
  }

  override def isUserPresent(id: String): Future[Boolean] = get(id).map {
    case None => false
    case Some(value) => value.validToken
  }
}

class UserTable(tag: Tag) extends Table[(String, String, Boolean)](tag, "users") {

  def id = column[String]("id", O.PrimaryKey)

  def name = column[String]("name")

  def validToken = column[Boolean]("validToken")

  def * = (id, name, validToken)
}