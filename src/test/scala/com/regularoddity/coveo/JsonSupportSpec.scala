package com.regularoddity.coveo

import org.postgresql.geometric.PGpoint
import org.scalatest.{ Matchers, FlatSpec }
import spray.json.{ DefaultJsonProtocol, JsArray, JsNumber, JsObject, JsString }

class JsonSupportSpec extends FlatSpec with Matchers with JsonSupport with DefaultJsonProtocol {
  val testCity = City(1234, "Place", "ON", "Place, ON", "CA", new PGpoint(12.3, 45.6), 78.9, 0.123)
  val cityFields = Vector(
    "id" -> JsNumber(1234),
    "name" -> JsString("Place"),
    "stateProvince" -> JsString("ON"),
    "fullName" -> JsString("Place, ON"),
    "countryCode" -> JsString("CA"),
    "coordinates" -> new JsArray(Vector(JsNumber(12.3), JsNumber(45.6))),
    "distance" -> JsNumber(78.9),
    "score" -> JsNumber(0.123)
  )
  "A properly formatted JSON object" should "be cast to a City object if possible" in {
    val json = new JsObject(Map(cityFields: _*))
    val city = cityJsonFormat.read(json)
    city shouldBe testCity
  }
  "A City object" should "be castable to JSON" in {
    val json = cityJsonFormat.write(testCity)
    json.fields.toSet should contain allElementsOf (cityFields)
  }
  "A properly formatted JSON object" should "be cast to Seq of City objects if possible" in {
    val json = new JsArray(Vector(new JsObject(Map(cityFields: _*)), new JsObject(Map(cityFields: _*)),
      new JsObject(Map(cityFields: _*))))
    val cities = citiesJsonFormat.read(json)
    cities shouldBe a[Seq[_]]
    cities should contain(testCity)
    cities should have length (3)
  }
  "A Seq of City objects" should "be castable to JSON" in {
    val json = citiesJsonFormat.write(Seq(testCity, testCity, testCity))
    json shouldBe a[JsArray]
    json.elements should contain(new JsObject(Map(cityFields: _*)))
    json.elements should have length (3)
  }
}
