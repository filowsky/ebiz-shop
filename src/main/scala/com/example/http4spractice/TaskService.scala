package com.example.http4spractice

import java.util.UUID

import cats.Applicative
import cats.implicits._
import com.example.http4spractice.TaskService.domain._

import scala.collection.mutable

sealed trait TaskService[F[_]] {
  def get(id: String): F[Option[Task]]

  def add(description: String): F[Either[ErrorMsg, Task]]

  def updateStatus(id: String, status: Status): F[UpdateStatus]

  def updateDescription(id: String, description: String): F[UpdateStatus]

  def delete(id: TaskId): F[DeleteStatus]
}

object TaskService {

  object domain {
    type TaskId = String
    type ErrorMsg = String

    final case class Task(id: TaskId, description: String, status: Status)

    sealed trait Status

    case object ToDo extends Status

    case object InProgress extends Status

    case object Blocked extends Status

    case object Done extends Status

    sealed trait UpdateStatus

    final case class UpdateSuccess(id: TaskId) extends UpdateStatus

    final case class UpdateFailure(id: TaskId, msg: ErrorMsg) extends UpdateStatus

    sealed trait DeleteStatus

    final case class DeleteSuccess(id: TaskId) extends DeleteStatus

    final case class DeleteFailure(id: TaskId, msg: ErrorMsg) extends DeleteStatus

  }

  object infra {
    def impl[F[_] : Applicative]: TaskService[F] = new TaskService[F] {

      private val db: mutable.Map[String, Task] = mutable.HashMap.empty[String, Task]

      def get(id: TaskId): F[Option[Task]] = db.get(id).pure[F]

      def add(description: String): F[Either[ErrorMsg, Task]] =
        UUID.randomUUID().toString.pure[F]
          .map { uuid =>
            db.put(uuid, Task(uuid, description, ToDo)) match {
              case None => db.get(uuid) match {
                case None => Left(s"Task not added, description: $description")
                case Some(task) => Right(task)
              }
              case Some(task) => Right(task)
            }
          }

      def updateStatus(id: TaskId, status: Status): F[UpdateStatus] = {
        db.get(id) match {
          case None => onUpdateOnAbsent(id)
          case Some(task) => onStatusChangedOnPresent(task, status)
        }
      }.pure[F]

      def updateDescription(id: String, description: String): F[UpdateStatus] = {
        db.get(id) match {
          case None => onUpdateOnAbsent(id)
          case Some(task) => onDescriptionChangedOnPresent(task, description)
        }
      }.pure[F]

      def delete(id: TaskId): F[DeleteStatus] = onDelete(id).pure[F]

      private def onDelete(id: TaskId): DeleteStatus = db.remove(id) match {
        case None => DeleteFailure(id, "Task not deleted")
        case Some(_) => DeleteSuccess(id)
      }

      private def onDescriptionChangedOnPresent(task: Task, newDescription: String): UpdateStatus =
        db.put(task.id, task.copy(description = newDescription)) match {
          case None => UpdateFailure(task.id, "Couldn't change description")
          case Some(task) => UpdateSuccess(task.id)
        }

      private def onUpdateOnAbsent(id: TaskId): UpdateStatus =
        UpdateFailure(id, s"Task with id: $id not found")

      private def onStatusChangedOnPresent(task: Task, newStatus: Status): UpdateStatus =
        db.put(task.id, task.copy(status = newStatus)) match {
          case None => UpdateFailure(task.id, "Couldn't change status")
          case Some(task) => UpdateSuccess(task.id)
        }
    }
  }

}