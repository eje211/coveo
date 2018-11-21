package com.regularoddity.coveo

import org.scalatest._
import java.sql.DriverManager

import scala.util.Try

/**
 * This is a read-only application. If the database were to be updated, it would be outside of this service.
 * The tests therefore only have to check read features. Transactions are not necessary and for these operations
 * concurrence is handled automatically by Postgres.
 * Slick has its own test kit, but it seems to work only with JUnit.
 */
class DatabaseConnectionSpec extends FlatSpec with Matchers {
  val conf = new ConfigurationHandle("test.conf").configuration
  val driver = Class.forName("org.postgresql.Driver")
  val connection = Try(DriverManager.getConnection(
    conf.getString("database.postgres_url"),
    conf.getString("database.name"),
    conf.getString("database.password")
  ))

  "The database" should "contain all the required views and tables" in {
    val template =
      """
        |SELECT EXISTS
        |(
        |	SELECT 1
        |	FROM information_schema.tables
        |	WHERE table_schema = 'public'
        |	AND table_name = '%s'
        |);
      """.stripMargin
    val tables =
      "cities" ::
        "cities_v" ::
        "canadian_provinces" :: Nil
    tables foreach { table =>
      connection.map(c => {
        val statement = c.createStatement()
        val resultSet = statement.executeQuery(template.format(table))
        while (resultSet.next()) {
          val exists = resultSet.getBoolean("exists")
          exists shouldBe true
        }
      })
    }
  }

  "Each table" should "have the correct columns" in {
    val columns = Iterator(
      "cities" -> Iterator(
        ("id", "integer"),
        ("name", "character varying"),
        ("asciiname", "character varying"),
        ("alternatename", "character varying"),
        ("latitude", "real"),
        ("longitude", "real"),
        ("feature_class", "character"),
        ("feature_code", "character varying"),
        ("country_code", "character"),
        ("cc2", "character varying"),
        ("admin_code1", "character varying"),
        ("admin_code2", "character varying"),
        ("admin_code3", "character varying"),
        ("admin_code4", "character varying"),
        ("population", "bigint"),
        ("elevation", "integer"),
        ("dem", "integer"),
        ("timezone", "character varying"),
        ("modified", "date")
      ),
      "cities_v" -> Iterator(
        ("id", "integer"),
        ("name", "character varying"),
        ("state_province", "character varying"),
        ("full_name", "text"),
        ("country_code", "character"),
        ("location", "point")
      ),
      "canadian_provinces" -> Iterator(
        ("id", "character varying"),
        ("postal_abbreviation", "character varying"),
        ("name", "character varying")
      )
    )
    val template =
      """
        |SELECT column_name, data_type
        |FROM information_schema.columns
        |WHERE table_schema = 'public'
        |  AND table_name   = '%';
      """.stripMargin
    while (columns.hasNext) {
      val column = columns.next()
      connection.map(c => {
        val statement = c.createStatement()
        val resultSet = statement.executeQuery(template.format(column._1))
        while (resultSet.next() && column._2.hasNext) {
          val check = column._2.next()
          val columnName = resultSet.getBoolean("column_name")
          val dataType = resultSet.getBoolean("data_type")
          columnName shouldBe check._1
          dataType shouldBe check._2
        }
      })
    }
  }
}
