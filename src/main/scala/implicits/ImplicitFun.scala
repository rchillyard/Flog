package implicits

import scala.language.implicitConversions

// Type class
trait Showable[T] {
  def show(t: T): Unit
}

case class Holiday(day: String, name: String)

object Holiday {
  implicit object ShowableHoliday extends Showable[Holiday] {
    def show(t: Holiday): Unit = println(t)
  }
}

object ImplicitFun extends App {

  implicit class RedSox(w: String) {
    def showIt(s: String): Unit = println(s"$w:$s")
  }

  implicit def f(w: String): Holiday = Holiday(w, "Patriot's day")

  "Hello" showIt "Goodbye"

  println("Monday".day)

  def hello(s: String)(implicit w: String, y: Int): Unit = {
    println(s"Hello, $s: $w")
  }

  implicit val z: String = "Patriot's Day"
  implicit val q: Int = 42
  hello("Monday")

  val pDay = Holiday("Monday", "Patriot's Day")

    show(pDay)

  def show[T: Showable](t: T): Unit = implicitly[Showable[T]].show(t)
}
