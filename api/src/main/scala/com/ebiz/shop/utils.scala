package com.ebiz.shop

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

object utils {
  def await[X](f: Future[X]): X = {
    Await.result(f, Duration.Inf)
  }
}
