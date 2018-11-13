package com.regularoddity.coveo

import org.postgresql.geometric.PGpoint

object DatabaseConnection extends DatabaseHelpers with PGPoint {
  import slick.jdbc.PostgresProfile.api._

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

  def getCities(coordinate: PGpoint, location: String, limit: Int, fuzzy: Boolean = true) = {
    val query = getQuery(coordinate, location, limit)
    db.run(query.result)
      .map(_.map(city => City(city._1, city._2, city._3, city._4, city._5, city._6)))
  }

}

case class City(
  id: Long,
  name: String,
  stateProvince: String,
  fullName: String,
  countryCode: String,
  location: PGpoint
)

/*

Score search:

SELECT
  full_name,
  ls.lscore,
  m.maxscore
FROM
  cities_v
JOIN (
  SELECT
    MAX(levenshtein(full_name, 'ville')) AS maxscore
  FROM
    cities_v
  GROUP BY
    TRUE
  ) AS m
  ON TRUE
JOIN LATERAL (
  SELECT
    id, levenshtein(full_name, 'ville') AS lscore
  FROM
    cities_v
  ) AS ls
  ON ls.id = cities_v.id
ORDER BY ls.lscore
LIMIT 10;

 */