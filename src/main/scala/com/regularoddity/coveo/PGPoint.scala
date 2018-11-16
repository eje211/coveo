package com.regularoddity.coveo

import java.sql.{ PreparedStatement, ResultSet }

import org.postgresql.geometric.PGpoint
import slick.ast
import slick.ast.{ FieldSymbol, ScalaType, Type }
import slick.jdbc.{ GetResult, PositionedResult }
import slick.util.ConstArray

import scala.reflect.ClassTag
import scala.util.Try

/**
 * The implicits necessary for extracting data of type `PGpoint` from the database using Slick's DSL.
 */
trait PGPoint {
  implicit object GetPGPoint extends GetResult[PGpoint] {
    def apply(rs: PositionedResult) = {
      val arguments = rs.nextString().substring(1).dropRight(1).split(',').map(_.toDouble).toVector
      new PGpoint(arguments(0), arguments(1))
    }
  }

  implicit val CityClassTag = new ClassTag[City]() {
    override def runtimeClass: Class[City] = classOf[City]
    override def unapply(x: Any): Option[City] = x match {
      case (id: Long,
        name: String,
        stateProvince: String,
        fullName: String,
        countryCode: String,
        location: PGpoint,
        distance: Double,
        score: Double
        ) => Some(City(id, name, stateProvince, fullName, countryCode, location, distance, score))
    }
  }

  implicit val PGpointClassTag = new ClassTag[PGpoint]() {
    override def runtimeClass: Class[PGpoint] = classOf[PGpoint]
    override def unapply(xx: Any): Option[PGpoint] = xx match {
      case (x: Double, y: Double) => Some(new PGpoint(x, y))
      case _ => None
    }
  }

  // The compiler won't extend a `type` reference. Casting these to a trait solves this problem.
  trait TypedTypePGPoint extends slick.jdbc.JdbcType[org.postgresql.geometric.PGpoint]
    with slick.ast.BaseTypedType[org.postgresql.geometric.PGpoint]

  class PointType() extends TypedTypePGPoint {
    override def sqlType: Int = 0
    override def sqlTypeName(size: Option[FieldSymbol]): String = "point"
    override def setValue(v: PGpoint, p: PreparedStatement, idx: Int): Unit = p.setObject(idx, v)
    override def setNull(p: PreparedStatement, idx: Int): Unit = throw new IllegalStateException("PGpoint cannot be null.")
    override def getValue(r: ResultSet, idx: Int): PGpoint = r.getObject(idx).asInstanceOf[PGpoint]
    override def wasNull(r: ResultSet, idx: Int): Boolean = false
    override def updateValue(v: PGpoint, r: ResultSet, idx: Int): Unit = r.updateObject(idx, v)
    override def valueToSQLLiteral(value: PGpoint): String = s"""point(${value.x},${value.y})"""
    override def hasLiteralForm: Boolean = true
    override def scalaType = new ScalaType[PGpoint]() {
      override def nullable: Boolean = true
      override def ordered: Boolean = false
      override def scalaOrderingFor(ord: ast.Ordering) = new Ordering[PGpoint] {
        override def compare(x: PGpoint, y: PGpoint): Int =
          (math.sqrt(math.pow(x.x, 2.0) + math.pow(x.y, 2.0)) -
            math.sqrt(math.pow(y.x, 2.0) + math.pow(y.y, 2.0))).toInt
        def classTag: ClassTag[PGpoint] = PGpointClassTag
      }
      override def children = ConstArray.empty
      override def mapChildren(f: Type => Type): Type = children.map(f).apply(0)
      override def classTag: ClassTag[PGpoint] = PGpointClassTag
    }
    override def classTag: ClassTag[PGpoint] = PGpointClassTag
  }

}

object PGPoint {

  /**
   * Implicitly transform a  `String` into a `PGpoint`, if possible.
   * This called with the implicit method [[PointStringConverterFrom.toPointOpt]] as in `"2.6666,3.868".toPointOpt`.
   *
   * @param s The `String` to transform. It must be in the form "float,float", for example "2.55,7.979" or "7,0.06".
   * @return The `PGpoint` wrapped in an `Option`, if one could be made.
   */
  implicit def PointConverter(s: String) = new PointStringConverterFrom(s)

  /**
   * Implicitly transform a `Vector[Double]` into a `PGpoint`, if possible.
   * This called with the implicit method [[PointStringConverterFrom.toPointOpt]].
   *
   * @param s The `Vector[Double]` to transform.
   * @return The `PGpoint` wrapped in an `Option`, if one could be made.
   */
  implicit def PointConverter(v: Vector[Double]) = new PointVectorConverterFrom(v)

  /**
   * Implicitly convert a `PGpoint` to other formats.
   */
  implicit def PointConverter(p: PGpoint): PointConverterTo = new PointConverterTo(p)

  /**
   * Make an object that can transform a `String` into a `PGpoint`, if possible.
   * @param s The string to be converted into a `PGpoint`.
   */
  class PointStringConverterFrom(s: String) {

    /**
     * Convert from `String` to `PGpoint`.
     * @return `Some[PGpoint]` if one could be made, or `None` otherwise.
     */
    def toPointOpt: Option[PGpoint] =
      Try({
        val parts = s.split(",").map(_.toDouble).toVector
        new PGpoint(parts(0), parts(1))
      }).toOption
  }

  /**
   * Make an object that can transform a `Vector[Double]` into a `PGpoint`, if possible.
   * @param s The string to be converted into a `PGpoint`.
   */
  class PointVectorConverterFrom(v: Vector[Double]) {

    /**
     * Convert from `String` to `PGpoint`.
     * @return `Some[PGpoint]` if one could be made, or `None` otherwise.
     */
    def toPointOpt: Option[PGpoint] =
      Try({
        new PGpoint(v(0), v(1))
      }).toOption
  }
  /**
   * Make an object that can a `PGpoint` into other types.
   * @param s The string to be converted into a `PGpoint`.
   */

  class PointConverterTo(p: PGpoint) {
    /**
     * Covert the `PGpoint` to a `Vector[Double]`.
     * For example `new PGpoint(2.0, 3.0)` would become `Vector(2.0, 3.0)`.
     *
     * @return The values inside the `PGpoint` as a `Vector[Double]`.
     */
    def toVector = Vector(p.x, p.y)
  }

}
