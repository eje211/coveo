package com.regularoddity.coveo

import org.postgresql.geometric.PGpoint
import slick.lifted.SimpleExpression
import slick.jdbc.PostgresProfile.api._

/**
 * Extra helper methods for Slick database classes.
 */
trait DatabaseHelpers {
  /**
   * Make a `String` fuzzy for a `LIKE` database search.
   * @param s The `String` the database will search for.
   * @return a `String` that will perform a fuzzy version of that match.
   */
  def fuzzyString(s: String): String = s"${s.split("").mkString("%")}"

  /**
   * Adds an operator to the Slick DSL for Postgres for the distance operator between two points.
   */
  val distance = SimpleExpression.binary[PGpoint, PGpoint, Double] {
    (point1, point2, queryBuilder) =>
      queryBuilder.expr(point1)
      queryBuilder.sqlBuilder += " <-> "
      queryBuilder.expr(point2)
  }
}
