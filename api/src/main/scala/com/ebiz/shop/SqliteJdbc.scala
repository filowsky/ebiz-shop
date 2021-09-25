package com.ebiz.shop

import java.sql.{DriverManager, PreparedStatement}
import scala.util.{Failure, Success, Try}

object SqliteJdbc {
  def init(fileName: String): Unit = {
    val url = "jdbc:sqlite:" + fileName
    Try {
      val conn = DriverManager.getConnection(url)
      val meta = conn.getMetaData
      println("The driver name is " + meta.getDriverName)
      println("A new database has been created.")
      println("Connection to SQLite has been established.")

      val sql =
        """
          |CREATE TABLE IF NOT EXISTS products (
          | id integer PRIMARY KEY,
          | name text NOT NULL,
          | description string,
          | category string
          |);
          |""".stripMargin

      conn.createStatement().execute(sql)
      val insert = "INSERT INTO products(name,description,category) VALUES(?,?,?)"
      val pstmt: PreparedStatement = conn.prepareStatement(insert)
      pstmt.setString(1, "name")
      pstmt.setString(2, "desc")
      pstmt.setString(3, "cat")
      pstmt.executeUpdate()

      val select = "SELECT id, name, description, category FROM products"
      val stmt = conn.createStatement
      val rs = stmt.executeQuery(select)
      while (rs.next)
        println(rs.getInt("id") + "\t" + rs.getString("name") + "\t" + rs.getString("description") + "\t" + rs.getString("category"))
    } match {
      case Success(value) =>
        println(value)
      case Failure(ex) =>
        println(ex.getMessage)
    }
  }
}
