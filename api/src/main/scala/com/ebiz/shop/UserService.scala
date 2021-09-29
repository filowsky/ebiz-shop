package com.ebiz.shop

import cats.Applicative
import cats.implicits.catsSyntaxApplicativeId
import com.ebiz.shop.ProductService.domain.ErrorMsg

sealed trait UsersService[F[_]] {
  def save(user: User): F[Either[ErrorMsg, Unit]]

  def get(id: String): F[Option[User]]

  def validateToken(id: String): F[Boolean]

  def updateValidation(id: String, isValid: Boolean): F[Boolean]

  def isUserPresent(id: String): F[Boolean]
}

case class User(id: String, name: String, validToken: Boolean, token: String)

object UsersService {
  object infra {
    def synchronousUsersService[F[_] : Applicative](usersRepository: SlickUsersRepository) = new UsersService[F] {
      override def save(user: User): F[Either[ErrorMsg, Unit]] = utils.await(usersRepository.save(user)).pure[F]

      override def get(id: String): F[Option[User]] = utils.await(usersRepository.get(id)).pure[F]

      override def validateToken(id: String): F[Boolean] = utils.await(usersRepository.validateToken(id)).pure[F]

      override def updateValidation(id: String, isValid: Boolean): F[Boolean] = utils.await(usersRepository.updateValidation(id, isValid)).pure[F]

      override def isUserPresent(id: String): F[Boolean] = utils.await(usersRepository.isUserPresent(id)).pure[F]
    }
  }
}
