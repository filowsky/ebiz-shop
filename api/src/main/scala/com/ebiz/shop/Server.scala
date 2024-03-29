package com.ebiz.shop

import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import fs2.Stream
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.{CORS, Logger}
import slick.jdbc.JdbcBackend.Database

import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future
import scala.concurrent.duration.Duration

object Server {

  def stream[F[_] : ConcurrentEffect](implicit T: Timer[F], C: ContextShift[F]): Stream[F, Nothing] = {
    val products = ProductService.infra.synchronousProductService[F](
      new SlickProductsRepository(Database.forConfig("products"))
    )
    val users = UsersService.infra.synchronousUsersService[F](
      new SlickUsersRepository(Database.forConfig("users"))
    )
    val httpApp = Logger.httpApp(logHeaders = true, logBody = true)(
      CORS(ShopRoutes.productsRoutes[F, Future](products, users)).orNotFound
    )
    BlazeServerBuilder[F](global)
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(httpApp)
      .withIdleTimeout(Duration(5, TimeUnit.SECONDS))
      .serve
  }.drain
}
