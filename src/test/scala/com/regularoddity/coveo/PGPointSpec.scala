package com.regularoddity.coveo

import org.postgresql.geometric.PGpoint
import org.scalatest.{ Matchers, WordSpec }

class PGPointSpec extends WordSpec with Matchers {

  import com.regularoddity.coveo.PGPoint._

  "The PGpoint converters PGpoint to Vector" should {
    "produce a Vector of Doubles" in {
      val point = new PGpoint(1.0, 2.0)
      val vector = point.toVector
      vector.map(_.getClass) should contain only classOf[Double]
    }
    "produce a Vector of length 2" in {
      val point = new PGpoint(1.0, 2.0)
      val vector = point.toVector
      vector should have size 2
    }
    "contain the same values as the original point in the same order" in {
      val coordinates = Seq(0.3, 1.6)
      val point = new PGpoint(coordinates(0), coordinates(1))
      val vector = point.toVector
      vector should contain theSameElementsInOrderAs coordinates
    }
  }

  "The PGpoint converters from Vector to PGpoint" should {
    "return a Some(PGpoint) for a Vector of Doubles" in {
      val vector = Vector(8.9, 0.0)
      val point = vector.toPointOpt
      point should contain(new PGpoint(8.9, 0.0))
    }
    "fail if the Vector is too short" in {
      val vector = Vector(8.9)
      val point = vector.toPointOpt
      point shouldBe None
      val emptyVector = Vector.empty
      emptyVector.toPointOpt shouldBe None
    }
    "ignore the overflow values if the Vector is too long" in {
      val vector = Vector(8.9, 0.0, 1.0)
      val point = vector.toPointOpt
      point should contain(new PGpoint(8.9, 0.0))
    }
  }

  "The PGpoint converters String to PGpoint" should {
    "convert a properly formatted String to a PGpoint" in {
      val point1 = "8.8, 0.0".toPointOpt
      point1 should contain(new PGpoint(8.8, 0.0))
      val point2 = "  8.8, 0.0  ".toPointOpt
      point2 should contain(new PGpoint(8.8, 0.0))
      val point3 = "8, 0.0  ".toPointOpt
      point3 should contain(new PGpoint(8, 0.0))
      val point4 = "8.8  , 0.0".toPointOpt
      point4 should contain(new PGpoint(8.8, 0.0))
      val point5 = "8.8,0.0".toPointOpt
      point5 should contain(new PGpoint(8.8, 0.0))
    }
    "convert an improperly formatted String to a None" in {
      val point1 = "a8.8, 0.0".toPointOpt
      point1 shouldBe None
      val point2 = "8.8 0.0".toPointOpt
      point2 shouldBe None
      val point3 = "8.80.0".toPointOpt
      point3 shouldBe None
      val point4 = "0.0".toPointOpt
      point4 shouldBe None
      val point5 = "0, 9.8a".toPointOpt
      point5 shouldBe None
      val point6 = "hello".toPointOpt
      point6 shouldBe None
      val point7 = "".toPointOpt
      point7 shouldBe None
      val point8 = "0.6,,8.9".toPointOpt
      point8 shouldBe None
      val point9 = ",".toPointOpt
      point9 shouldBe None
    }
  }

}
