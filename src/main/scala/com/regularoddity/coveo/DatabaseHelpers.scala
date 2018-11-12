package com.regularoddity.coveo

import org.postgresql.geometric.PGpoint
import slick.lifted.SimpleExpression
import slick.jdbc.PostgresProfile.api._

trait DatabaseHelpers {
  def fuzzyString(s: String): String = s"%${s.split("").mkString("%")}%"

  val distance = SimpleExpression.binary[PGpoint, PGpoint, Double] {
    (point1, point2, queryBuilder) =>
      queryBuilder.expr(point1)
      queryBuilder.sqlBuilder += " <-> "
      queryBuilder.expr(point2)
  }
}
