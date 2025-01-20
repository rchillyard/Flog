/*
 * Copyright (c) 2021. Phasmid Software
 */

package com.phasmidsoftware.flog

import java.time.LocalDateTime
import org.scalatest.flatspec
import org.scalatest.matchers.should
import scala.util.Try

//noinspection ScalaStyle
class LoggablesSpec extends flatspec.AnyFlatSpec with should.Matchers with Loggables {

  behavior of "Loggables"

  it should "optionLoggable" in {
    val target = optionLoggable[Int]
    target.toLog(Some(42)) shouldBe "Some(42)"
    target.toLog(None) shouldBe "None"
  }

  it should "mapLoggable" in {
    val target = mapLoggable[String, Int]()
    target.toLog(Map("x" -> 1, "y" -> 2)) shouldBe "{x:1,y:2}"
  }

  it should "iterableLoggable0" in {
    val target = iterableLoggable[Int]("{}")
    target.toLog(Seq(42)) shouldBe "{42}"
    target.toLog(Seq(42, 99)) shouldBe "{42, 99}"
    target.toLog(Seq(42, 99, 101)) shouldBe "{42, 99, 101}"
  }

  it should "iterableLoggable1" in {
    val target = iterableLoggable[Int]()
    target.toLog(Seq(42, 99, 101, 357)) shouldBe "[42, 99, 101, 357]"
  }

  it should "iterableLoggable2" in {
    val target = iterableLoggable[Int]()
    target.toLog(Seq(42, 99, 101, 357, 911)) shouldBe "[42, 99, 101, ... (1 element), ... 911]"
  }

  it should "iterableLoggable3" in {
    val target = iterableLoggable[Int]("<>", 4)
    target.toLog(Seq(42, 99, 101, 357, 911, 1001, 2048, 4192)) shouldBe "<42, 99, 101, 357, ... (3 elements), ... 4192>"
  }

  it should "tryLoggable" in {
    val target = tryLoggable[Int]
    target.toLog(Try("1".toInt)) shouldBe "Success(1)"
    target.toLog(Try(throw new NoSuchElementException("bad"))) shouldBe "Failure(bad)"
  }

  it should "eitherLoggable" in {
    val x: Either[Int, Double] = Right(Math.PI)
    val y: Either[Int, Double] = Left(1)
    val target = eitherLoggable[Int, Double]
    target.toLog(x) shouldBe "Right(3.141592653589793)"
    target.toLog(y) shouldBe "Left(1)"
  }

  // TODO invoke these via PrivateMethodTester...
  //  it should "valueToLog" in {
  //    valueToLog[Int, (Int, Int)]((42, 99), 0) shouldBe "42"
  //    valueToLog[Int, (Int, Int)]((42, 99), 1) shouldBe "99"
  //  }

  it should "loggable1" in {
    case class Onesy(x: Int)
    val target = loggable1(Onesy.apply)
    target.toLog(Onesy(42)) shouldBe "Onesy(x:42)"
  }

  it should "loggable2" in {
    case class Twosy(x: Int, y: Boolean)
    val target = loggable2(Twosy.apply)
    target.toLog(Twosy(42, y = true)) shouldBe "Twosy(x:42,y:true)"
  }

  it should "loggable3" in {
    case class Threesy(x: Int, y: Boolean, z: Double)
    val target = loggable3(Threesy.apply)
    target.toLog(Threesy(42, y = true, 3.1415927)) shouldBe "Threesy(x:42,y:true,z:3.1415927)"
  }

  it should "loggable4" in {
    case class Foursy(x: Int, y: Boolean, z: Double, q: String)
    val target = loggable4(Foursy.apply)
    target.toLog(Foursy(42, y = true, 3.1415927, "x")) shouldBe "Foursy(x:42,y:true,z:3.1415927,q:x)"
  }

  it should "loggable4 with explicit field names" in {
    case class Foursy(x: Int, y: Boolean, z: Double, q: String)
    val target = loggable4(Foursy.apply, Seq("x", "y", "z", "q"))
    target.toLog(Foursy(42, y = true, 3.1415927, "x")) shouldBe "Foursy(x:42,y:true,z:3.1415927,q:x)"
  }

  it should "loggable5" in {
    case class Fivesy(x: Int, y: Boolean, z: Double, q: String, r: BigInt)
    val loggable: Loggable[Fivesy] = loggable5(Fivesy.apply)
    loggable.toLog(Fivesy(42, y = true, 3.1415927, "x", BigInt(99))) shouldBe "Fivesy(x:42,y:true,z:3.1415927,q:x,r:99)"
  }

  it should "loggable6" in {
    case class Sixy(a: Option[Int], x: Int, y: Boolean, z: Double, q: String, r: BigInt)
    val loggable: Loggable[Sixy] = loggable6(Sixy.apply)
    loggable.toLog(Sixy(Some(1), 42, y = true, 3.1415927, "x", BigInt(99))) shouldBe "Sixy(a:Some(1),x:42,y:true,z:3.1415927,q:x,r:99)"
  }

  it should "loggable7" in {
    case class Complicated(a: Option[Int], b: Option[Double], x: Int, y: Boolean, z: Double, q: String, r: BigInt)
    // NOTE: we must explicitly include an implicit Loggable[Option[Double]] because that particular value is not imported from Loggable._
    implicit val z: Loggable[Option[Double]] = optionLoggable[Double]
    val loggable: Loggable[Complicated] = loggable7(Complicated.apply)
    loggable.toLog(Complicated(Some(1), Some(Math.PI), 42, y = true, 3.1415927, "x", BigInt(99))) shouldBe "Complicated(a:Some(1),b:Some(3.141592653589793),x:42,y:true,z:3.1415927,q:x,r:99)"
  }

  it should "loggable8" in {
    case class Complicated8(name: String, a: Option[Int], b: Option[Double], x: Int, y: Boolean, z: Double, q: BigDecimal, r: BigInt)
    // NOTE: we must explicitly include an implicit Loggable[Option[Double]] because that particular value is not imported from Loggable._
    implicit val z: Loggable[Option[Double]] = optionLoggable[Double]
    val loggable: Loggable[Complicated8] = loggable8(Complicated8.apply)
    val complicated = Complicated8("Robin", Some(1), Some(Math.PI), 42, y = true, 3.1415927, BigDecimal("3.1415927"), BigInt(99))
    loggable.toLog(complicated) shouldBe "Complicated8(name:Robin,a:Some(1),b:Some(3.141592653589793),x:42,y:true,z:3.1415927,q:3.1415927,r:99)"
  }

  it should "loggable9" in {
    case class Complicated9(name: String, date: LocalDateTime, a: Option[Int], b: Option[Double], x: Int, y: Boolean, z: Double, q: BigDecimal, r: BigInt)
    // NOTE: we must explicitly include an implicit Loggable[Option[Double]] because that particular value is not imported from Loggable._
    implicit val z1: Loggable[Option[Double]] = optionLoggable[Double]
    val loggable: Loggable[Complicated9] = loggable9(Complicated9.apply) // finds loggableAny
    val complicated = Complicated9("Robin", LocalDateTime.of(2021, 1, 1, 12, 0), Some(1), Some(Math.PI), 42, y = true, 3.1415927, BigDecimal("3.1415927"), BigInt(99))
    loggable.toLog(complicated) shouldBe "Complicated9(name:Robin,date:2021-01-01T12:00,a:Some(1),b:Some(3.141592653589793),x:42,y:true,z:3.1415927,q:3.1415927,r:99)"
  }
}
