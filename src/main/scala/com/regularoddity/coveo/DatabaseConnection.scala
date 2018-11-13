package com.regularoddity.coveo

import org.postgresql.geometric.PGpoint
import slick.driver
import slick.jdbc.{ GetResult, PositionedParameters, SetParameter }

object DatabaseConnection extends DatabaseHelpers with PGPoint {
  import slick.jdbc.PostgresProfile.api._
  import PGPoint._

  implicit val ExecutionContext = QuickstartServer.system.dispatcher

  implicit val PGpointType = new PointType()

  val db = Database.forURL("jdbc:postgresql://localhost/coveo", driver = "org.postgresql.Driver")

  class Cities(tag: Tag) extends Table[(Long, String, String, String, String, PGpoint)](tag, "cities_v") {

    def id = column[Long]("id", O.PrimaryKey)
    def name = column[String]("name")
    def stateProvince = column[String]("state_province")
    def fullName = column[String]("full_name")
    def countryCode = column[String]("country_code")
    def location = column[PGpoint]("location")

    def * = (id, name, stateProvince, fullName, countryCode, location)
  }

  implicit val cityResult = GetResult[City](r => {
    City(
      r.nextInt(), r.nextString(), r.nextString(), r.nextString(), r.nextString(),
      r.nextString().dropRight(1).drop(1).toPointOpt.get, r.nextDouble(), r.nextDouble()
    )
  })

  val cities = TableQuery[Cities]

  def getQuery(coordinate: PGpoint, location: String, limit: Int, fuzzy: Boolean = true) =
    for (
      result <- cities.map(cities => (cities.id, distance(cities.location, coordinate)))
        .join(cities).on(_._1 === _.id)
        .filter(_._2.fullName like s"%${if (fuzzy) fuzzyString(location) else location}%")
        .sortBy(_._1._2).take(limit)
    ) yield {
      result._2
    }

  //  def getCities(coordinate: PGpoint, location: String, limit: Int, fuzzy: Boolean = true) = {
  //    val query = getQuery(coordinate, location, limit)
  //    db.run(query.result)
  //      .map(_.map(city => City(city._1, city._2, city._3, city._4, city._5, city._6)))
  //  }

  def getScore(coordinate: PGpoint, location: String, limit: Long, fuzzy: Boolean = true) = {
    val locationStr = s"%${if (fuzzy) fuzzyString(location) else location}%"
    val pointValue = s"(${coordinate.x},${coordinate.y})"
    val query = sql"""
         |SELECT
         |  cities_v.id,
         |  name,
         |  state_province,
         |  full_name,
         |  country_code,
         |  location,
         |  distance,
         |  log((m.max_score + 2 - (m.min_score * 0.99))::numeric,
         |    (m.max_score + 2 - lat_data.score)::numeric) *
         |  log((m.max_distance + 2 - (m.min_distance * 0.99))::numeric,
         |    (m.max_distance + 2 - lat_data.distance)::numeric) as score
         |FROM
         |  cities_v
         |JOIN (
         |  SELECT
         |    MAX(levenshtein(full_name, $locationStr)) AS max_score,
         |    MIN(levenshtein(full_name, $locationStr)) as min_score,
         |    MIN($pointValue::point <-> location) as min_distance,
         |    MAX($pointValue::point <-> location) as max_distance
         |  FROM
         |    cities_v
         |  WHERE
         |    full_name ILIKE $locationStr
         |  GROUP BY
         |    TRUE
         |  ) AS m
         |  ON TRUE
         |JOIN LATERAL (
         |  SELECT
         |    id,
         |    levenshtein(full_name, $locationStr) AS score,
         |    $pointValue::point <-> location as distance
         |  FROM
         |    cities_v
         |  ) AS lat_data
         |  ON lat_data.id = cities_v.id
         |WHERE
         |  full_name ILIKE $locationStr
         |ORDER BY score DESC
         |LIMIT $limit::bigint """.stripMargin.as[City]
    db.run(query)
    //    ran.failed.map(f => f.printStackTrace()).isCompleted
    //    ran.map(c => c.map(println(_))).isCompleted
  }

}

case class City(
  id: Long,
  name: String,
  stateProvince: String,
  fullName: String,
  countryCode: String,
  coordinates: PGpoint,
  distance: Double,
  score: Double
)

/*

Score search:



 */ 