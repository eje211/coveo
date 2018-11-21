package com.regularoddity.coveo

import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{ Matchers, WordSpec }

class CityRoutesSpec extends WordSpec with Matchers with ScalatestRouteTest with JsonSupport {
  import scala.collection.mutable
  import scala.collection.parallel.ParSeq
  import PGPoint._

  "An impossible string on the route for /suggestion" should {
    "return an empty result" in {
      Get("/suggestion?LocationName=xxxwwwxkt&startPoint-124.25257110595703,49.057979583740234&") -> runRoute ->
        check {
          responseAs[Seq[City]] shouldBe empty
        }
    }
  }
  "Valid search query parameters" should {
    "return JSON" in {
      Get("/suggestion?LocationName=ville&startPoint-124.25257110595703,49.057979583740234&") -> runRoute ->
        check {
          responseAs[Seq[City]] shouldBe a[Seq[_]]
        }
    }
    "have either more different names than their previous result or be further away" in {
      Get("/suggestion?LocationName=ville&startPoint-124.25257110595703,49.057979583740234") -> runRoute ->
        check {
          responseAs[Seq[City]].sliding(2).forall(c =>
            c(0).coordinates.abs > c(1).coordinates.abs ||
              levenshtein("ville", c(0).fullName) > levenshtein("ville", c(1).fullName)) shouldBe true
        }
      Get("/suggestion?LocationName=oak&startPoint=-122.25257110595703,31.057979583740234&limit=20") -> runRoute ->
        check {
          responseAs[Seq[City]].sliding(2).forall(c =>
            c(0).coordinates.abs > c(1).coordinates.abs ||
              levenshtein("ville", c(0).fullName) > levenshtein("oak", c(1).fullName)) shouldBe true
        }
      Get("/suggestion?LlocationName=louis&startPoint=-142.25257110595703,31.057979583740234&limit=20") -> runRoute ->
        check {
          responseAs[Seq[City]].sliding(2).forall(c =>
            c(0).coordinates.abs > c(1).coordinates.abs ||
              levenshtein("ville", c(0).fullName) > levenshtein("louis", c(1).fullName)) shouldBe true
        }
    }
    "have fewer or as many results with fuzzy search turned off" in {
      Get("/suggestion?LocationName=ville&startPoint-124.25257110595703,49.057979583740234&limit=1000") ->
        runRoute -> check {
          responseAs[Seq[City]].size
        }.andThen(fuzz => Get(
          "/suggestion?LocationName=ville&startPoint-124.25257110595703,49.057979583740234&limit=1000&fuzzy=false"
        )
          -> runRoute -> check {
            responseAs[Seq[City]].size
          }.andThen(noFuzz => fuzz should be >= noFuzz))
      Get("/suggestion?LocationName=oak&startPoint=-122.25257110595703,31.057979583740234&limit=1000") ->
        runRoute -> check {
          responseAs[Seq[City]].size
        }.andThen(fuzz => Get(
          "/suggestion?LocationName=oak&startPoint=-122.25257110595703,31.057979583740234&limit=1000&fuzzy=false"
        )
          -> runRoute -> check {
            responseAs[Seq[City]].size
          }.andThen(noFuzz => fuzz should be >= noFuzz))
      Get("/suggestion?LlocationName=louis&startPoint=-142.25257110595703,31.057979583740234&limit=1000") ->
        runRoute -> check {
          responseAs[Seq[City]].size
        }.andThen(fuzz => Get(
          "/suggestion?LlocationName=louis&startPoint=-142.25257110595703,31.057979583740234&limit=1000&fuzzy=false"
        ) ->
          runRoute -> check {
            responseAs[Seq[City]].size
          }.andThen(noFuzz => fuzz should be >= noFuzz))
    }
  }

  /**
   * From https://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Functional_version
   */
  def levenshtein(s1: String, s2: String): Int = {
    val memoizedCosts = mutable.Map[(Int, Int), Int]()

    def lev: ((Int, Int)) => Int = {
      case (k1, k2) =>
        memoizedCosts.getOrElseUpdate((k1, k2), (k1, k2) match {
          case (i, 0) => i
          case (0, j) => j
          case (i, j) =>
            ParSeq(
              1 + lev((i - 1, j)),
              1 + lev((i, j - 1)),
              lev((i - 1, j - 1))
                + (if (s1(i - 1) != s2(j - 1)) 1 else 0)
            ).min
        })
    }

    lev((s1.length, s2.length))
  }
}
