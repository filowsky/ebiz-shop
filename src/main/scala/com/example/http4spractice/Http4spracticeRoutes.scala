package com.example.http4spractice

import cats.Applicative
import cats.effect.Sync
import cats.implicits._
import com.example.http4spractice.TaskService.domain._
import com.example.http4spractice.encoder._
import io.circe.generic.auto._
import io.circe.{Encoder, Json}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes}


object Http4spracticeRoutes {

  def taskServiceRoutes[F[_] : Sync](T: TaskService[F]): HttpRoutes[F] = {

    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "tasks" / id =>
        for {
          task <- T.get(id)
          resp <- task match {
            case None => NotFound(errorBody(s"Task $id not found"))
            case Some(task) => Ok(task)
          }
        } yield resp

      case req@POST -> Root / "tasks" =>
        implicit val decoder: EntityDecoder[F, TaskCreationRequest] = jsonOf[F, TaskCreationRequest]
        for {
          taskCreation <- req.as[TaskCreationRequest]
          addResponse <- T.add(taskCreation.description)
          resp <- addResponse match {
            case Right(value) => Ok(value)
            case Left(msg) => BadRequest(errorBody(msg))
          }
        } yield resp
      case req@PUT -> Root / "tasks" / "status" / id =>
        implicit val statusDecoder: EntityDecoder[F, TaskStatusUpdateRequest] = jsonOf[F, TaskStatusUpdateRequest]
        for {
          taskDescUpdate <- req.as[TaskStatusUpdateRequest]
          status: Either[String, Status] = taskDescUpdate.status match {
            case "ToDo" => Right(ToDo)
            case "InProgress" => Right(InProgress)
            case "Blocked" => Right(Blocked)
            case "Done" => Right(Done)
            case _ => Left(s"Invalid update status")
          }
          addResponse = status match {
            case Right(value) => T.updateStatus(id, value)
            case Left(msg) => UpdateFailure(id, msg).asInstanceOf[UpdateStatus].pure[F]
          }
          resp <- Ok(addResponse)
        } yield resp

      case req@PUT -> Root / "tasks" / "description" / id =>
        implicit val statusDecoder: EntityDecoder[F, TaskDescriptionUpdateRequest] = jsonOf[F, TaskDescriptionUpdateRequest]
        for {
          taskDescUpdate <- req.as[TaskDescriptionUpdateRequest]
          addResponse <- T.updateDescription(id, taskDescUpdate.description)
          resp <- Ok(addResponse)
        } yield resp
    }
  }

  private def errorBody(message: ErrorMsg): Json = Json.obj(
    ("message", Json.fromString(message))
  )
}

object encoder {
  implicit val taskEncoder: Encoder[Task] = new Encoder[Task] {
    final def apply(task: Task): Json =
      Json.obj(
        ("id", Json.fromString(task.id)),
        ("description", Json.fromString(task.description)),
        ("status", Json.fromString(task.status.toString)),
      )
  }

  implicit def taskEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Task] =
    jsonEncoderOf[F, Task]

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
}

final case class TaskCreationRequest(description: String)

final case class TaskStatusUpdateRequest(status: String)

final case class TaskDescriptionUpdateRequest(description: String)