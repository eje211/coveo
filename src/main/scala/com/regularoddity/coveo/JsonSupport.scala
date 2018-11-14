package com.regularoddity.coveo

import com.regularoddity.coveo.CityProtocol.{CitiesJsonFormat, CitiesObjJsonFormat}
import com.regularoddity.coveo.UserRegistryActor.ActionPerformed
import spray.json._
import PGPoint._

import scala.util.Try

//#json-support
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

/**
  * Add support for implicitly converting between JSON and known types in Spray.
  */
trait JsonSupport extends SprayJsonSupport {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  implicit val userJsonFormat = jsonFormat3(User)
  implicit val usersJsonFormat = jsonFormat1(Users)
  /**
    * Implicitly import the methods for converting between JSON and [[City]].
    */
  implicit val cityJsonFormat = CityProtocol
  /**
    * Implicitly import the methods for converting between JSON and `Seq[City]`.
    */
  implicit val citiesJsonFormat = CitiesJsonFormat
  /**
    * Implicitly import the methods for converting between JSON and [[Cities]].
    */
  implicit val citiesObjJsonFormat = CitiesObjJsonFormat

  implicit val actionPerformedJsonFormat = jsonFormat1(ActionPerformed)
}
//#json-support


/**
  * The implementation of the Slick conversations between JSON and various types related to [[City]].
  */
object CityProtocol extends DefaultJsonProtocol {

  /**
    * The Slick protocol to converting between JSON and [[City]].
    */
  implicit object CityJsonFormat extends RootJsonFormat[City] {
    /**
      * Convert from [[City]] to JSON.
      * @param c The [[City]] object to convert.
      * @return a JSON representation of the given [[City]] object.
      */
    def write(c: City) =
      JsObject(
        ("id", JsNumber(c.id)),
        ("fullName", JsString(c.fullName)),
        ("name", JsString(c.name)),
        ("stateProvince", JsString(c.stateProvince)),
        ("countryCode", JsString(c.countryCode)),
        ("coordinates", JsArray(c.coordinates.toVector.map(JsNumber(_)))),
        ("distance", JsNumber(c.distance)),
        ("score", JsNumber(c.score)),
      )

    /**
      * Convert from JSON to [[City]].
      * @param c The JSON object to convert.
      * @return a [[City]] representation of the given JSON object.
      */
    def read(value: JsValue) = value match {
      case jsObject: JsObject => jsObject.fields.toVector match {
        case Vector(
          ("id", JsNumber(id)),
          ("name", JsString(name)),
          ("stateProvince", JsString(stateProvince)),
          ("fullName", JsString(fullName)),
          ("countryCode", JsString(countryCode)),
          ("coordinates", JsArray(locationVector)),
          ("distance", JsNumber(distance)),
          ("score", JsNumber(score)),
        ) => Try(
          new City(id.intValue(), name, stateProvince, fullName, countryCode,
            locationVector.map(_.asInstanceOf[JsNumber].value.doubleValue()).toPointOpt.get,
            distance.doubleValue(), score.doubleValue())).toOption.getOrElse(
          deserializationError("Malformed data for city element.")
        )
        case _ => deserializationError("City expected")
      }
      case _ => deserializationError("City expected")
    }
  }

  /**
    * The Slick protocol to converting between JSON and [[City]].
    */
  implicit object CitiesJsonFormat extends RootJsonFormat[Seq[City]] {
    /**
      * Convert from `Seq[City]` to JSON.
      * @param c The `Seq[City]` object to convert.
      * @return a JSON representation of the given `Seq[City]` object.
      */
    def write(c: Seq[City]) =
      JsArray(c.map(v => CityJsonFormat.write(v)): _*)

    /**
      * Convert from JSON to `Seq[City]`.
      * @param c The JSON object to convert.
      * @return a `Seq[City]` representation of the given JSON object.
      */
    def read(value: JsValue): Seq[City] = value match {
      case JsArray(cities: Vector[JsValue]) =>
        cities.map(c => CityJsonFormat.read(c))
      case _ => deserializationError("City expected")
    }
  }

  /**
    * The Slick protocol to converting between JSON and [[Cities]].
    */
  implicit object CitiesObjJsonFormat extends RootJsonFormat[Cities] {    /**
    * Convert from [[Cities]] to JSON.
    * @param c The [[Cities]] object to convert.
    * @return a JSON representation of the given [[Cities]] object.
    */
    def write(c: Cities) =
      CitiesJsonFormat.write(c.cities)

    /**
      * Convert from JSON to [[Cities]].
      * @param c The JSON object to convert.
      * @return a [[Cities]] representation of the given JSON object.
      */
    def read(value: JsValue): Cities = value match {
      case cities: JsArray =>
        Cities(CitiesJsonFormat.read(cities))
      case _ => deserializationError("City expected")
    }
  }
}