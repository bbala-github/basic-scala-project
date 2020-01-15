package hw

class HW {
  def say(): String = { "Hello World" }
}

object HW extends App {
  val hw = new HW
  println(hw.say())
}
