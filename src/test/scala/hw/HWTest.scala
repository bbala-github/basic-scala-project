package hw

import org.scalatest.{FunSpec, ShouldMatchers}

class HWTest extends FunSpec with ShouldMatchers {
  describe("HW test") {
    it("should say Hello World") {
      val hw = new HW
      hw.say() should be ("Hello World")
    }
  }
}
