package hw

class HW {
  def say(): String = { "Hello World" }
}

object HW extends App {
  override def main(args: Array[String]): Unit = {
    val hw = new HW
    println(hw.say())
  }
}
