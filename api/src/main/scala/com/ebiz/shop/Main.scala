package com.example.http4spractice

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    Http4spracticeServer.stream[IO].compile.drain.as(ExitCode.Success)
}