package com.example.http4spractice

import java.util.concurrent.TimeUnit

import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import fs2.Stream
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger

import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration.Duration

object Http4spracticeServer {

  def stream[F[_] : ConcurrentEffect](implicit T: Timer[F], C: ContextShift[F]): Stream[F, Nothing] = {
    val tasks = TaskService.infra.impl[F]
    val httpApp = Logger.httpApp(logHeaders = true, logBody = true)(
      Http4spracticeRoutes.taskServiceRoutes[F](tasks).orNotFound
    )
    BlazeServerBuilder[F](global)
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(httpApp)
      .withIdleTimeout(Duration(5, TimeUnit.SECONDS))
      .serve
  }.drain
}
