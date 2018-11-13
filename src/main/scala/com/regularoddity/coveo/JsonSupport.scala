package com.regularoddity.coveo

import com.regularoddity.coveo.CityProtocol.{CitiesJsonFormat, CitiesObjJsonFormat}
import com.regularoddity.coveo.UserRegistryActor.ActionPerformed
import spray.json._
import PGPoint._

import scala.util.Try

//#json-support
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  implicit val userJsonFormat = jsonFormat3(User)
  implicit val usersJsonFormat = jsonFormat1(Users)
  implicit val cityJsonFormat = CityProtocol
  implicit val citiesJsonFormat = CitiesJsonFormat
  implicit val citiesObjJsonFormat = CitiesObjJsonFormat

  implicit val actionPerformedJsonFormat = jsonFormat1(ActionPerformed)
}
//#json-support

object CityProtocol extends DefaultJsonProtocol {

  implicit object CityJsonFormat extends RootJsonFormat[City] {
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

  implicit object CitiesJsonFormat extends RootJsonFormat[Seq[City]] {
    def write(c: Seq[City]) =
      JsArray(c.map(v => CityJsonFormat.write(v)): _*)

    def read(value: JsValue): Seq[City] = value match {
      case JsArray(cities: Vector[JsValue]) =>
        cities.map(c => CityJsonFormat.read(c))
      case _ => deserializationError("City expected")
    }
  }

  implicit object CitiesObjJsonFormat extends RootJsonFormat[Cities] {
    def write(c: Cities) =
      CitiesJsonFormat.write(c.cities)

    def read(value: JsValue): Cities = value match {
      case cities: JsArray =>
        Cities(CitiesJsonFormat.read(cities))
      case _ => deserializationError("City expected")
    }
  }
}