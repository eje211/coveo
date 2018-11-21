package com.regularoddity.coveo

import org.scalatest.{ Matchers, WordSpec }

class DatabaseHelperSpec extends WordSpec with Matchers with DatabaseHelpers {
  "The fuzzy string maker" should {
    "add percent signs between each character of a string" in {
      fuzzyString("example") shouldBe "e%x%a%m%p%l%e"
    }
    "keep an empty string empty" in {
      fuzzyString("") shouldBe ""
    }
  }
}
