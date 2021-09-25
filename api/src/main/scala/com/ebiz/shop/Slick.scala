package com.ebiz.shop

import slick.jdbc.SQLiteProfile.api._

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}


class Movies(tag: Tag) extends Table[(String, String, String, String, String, String)](tag, "Movies") {
  def imdbID = column[String]("imdbID", O.PrimaryKey)

  def title = column[String]("Title")

  def year = column[String]("Year")

  def director = column[String]("Director")

  def actors = column[String]("Actors")

  def plot = column[String]("Plot")

  def * = (imdbID, title, year, director, actors, plot)
}

object SqliteSlickExample extends App {

  val movies = TableQuery[Movies]
  val db = Database.forConfig("movies")
  val setup = DBIO.seq(movies.schema.create)
  val setupFuture = db.run(setup)

  println("Done")

  val q1 = movies.map(m => m)
  db.stream(q1.result).foreach(println)
  val insert = DBIO.seq(
    movies += (UUID.randomUUID().toString, "Foo", "1970", "Bar", "Meh, Hmm", "Hmmmmmmm")
  )
  val f = db.run(insert)
  Thread.sleep(1000)
  f.onComplete {
    case Success(_) =>
      db.stream(q1.result).foreach(println)
      db.close
    case Failure(t) => println("An error has occurred: " + t.getMessage)
  }

}