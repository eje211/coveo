package com.regularoddity.coveo

import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{ Matchers, WordSpec }

class CityRoutesSpec extends WordSpec with Matchers with ScalatestRouteTest with JsonSupport {

  "The route for /suggestion" can {
    "An impossible string" should {
      "return an empty result" in {
        Get("/suggestion?LocationName=xxxwwwxkt&startPoint-124.25257110595703,49.057979583740234&") -> runRoute ->
          check {
            responseAs[Seq[City]] shouldBe empty
          }
      }
      "Valid search query parameters" should {
        "return the same result regardless of order" in {

        }
      }
    }
  }

}
