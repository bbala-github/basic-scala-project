package hw

import org.scalatest.{FunSpec, Matchers}

class HWTest extends FunSpec with Matchers {
  describe("HW test") {
    it("should say Hello World") {
      val hw = new HW
      hw.say() should be ("Hello World")
    }
  }
}
