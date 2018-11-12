package com.regularoddity.coveo

import com.regularoddity.coveo.CityProtocol.{ CitiesJsonFormat, CitiesObjJsonFormat }
import com.regularoddity.coveo.DatabaseConnection.City
import com.regularoddity.coveo.UserRegistryActor.ActionPerformed
import org.postgresql.geometric.PGpoint
import spray.json._

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

  implicit object CityJsonFormat extends RootJsonFormat[DatabaseConnection.City] {
    def write(c: City) =
      JsObject(
        ("id", JsNumber(c.id)),
        ("name", JsString(c.name)),
        ("stateProvince", JsString(c.stateProvince)),
        ("fullName", JsString(c.fullName)),
        ("countryCode", JsString(c.countryCode)),
        ("location", JsArray(Vector(JsNumber(c.location.x), JsNumber(c.location.y))))
      )

    def read(value: JsValue) = value match {
      case jsObject: JsObject => jsObject.fields.toVector match {
        case Vector(
          ("id", JsNumber(id)),
          ("name", JsString(name)),
          ("stateProvince", JsString(stateProvince)),
          ("fullName", JsString(fullName)),
          ("countryCode", JsString(countryCode)),
          ("location", JsArray(Vector(JsNumber(location1), JsNumber(location2))))) =>
          new City(id.intValue(), name, stateProvince, fullName, countryCode,
            new PGpoint(location1.doubleValue(), location2.doubleValue()))
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