/*
 * Copyright (c) 2019. Phasmid Software
 */

package com.phasmidsoftware.util

import org.scalatest.flatspec
import org.scalatest.matchers.should

//noinspection ScalaStyle
class LoggablesSpec extends flatspec.AnyFlatSpec with should.Matchers with Loggables {

  behavior of "Loggables"

  it should "optionLoggable" in {
    val target = optionLoggable[Int]
    target.toLog(Some(42)) shouldBe "Some(42)"
  }

  it should "mapLoggable" in {
    val target = mapLoggable[String, Int]()
    target.toLog(Map("x" -> 1, "y" -> 2)) shouldBe "{x:1,y:2}"
  }

  it should "sequenceLoggable" in {
    val target = listLoggable[Int]
    target.toLog(List(42, 99, 101)) shouldBe "[42, ... (1 elements), ... 101]"
  }

  it should "valueToLog" in {
    valueToLog[Int, (Int, Int)]((42, 99), 0) shouldBe "42"
    valueToLog[Int, (Int, Int)]((42, 99), 1) shouldBe "99"
  }

  it should "toLog1" in {
    case class Onesy(x: Int)
    val target = toLog1(Onesy)
    target.toLog(Onesy(42)) shouldBe "Onesy(x:42)"
  }

  it should "toLog2" in {
    case class Twosy(x: Int, y: Boolean)
    val target = toLog2(Twosy)
    target.toLog(Twosy(42, y = true)) shouldBe "Twosy(x:42,y:true)"
  }

  it should "toLog3" in {
    case class Threesy(x: Int, y: Boolean, z: Double)
    val target = toLog3(Threesy)
    target.toLog(Threesy(42, y = true, 3.1415927)) shouldBe "Threesy(x:42,y:true,z:3.1415927)"
  }

  it should "toLog4" in {
    case class Foursy(x: Int, y: Boolean, z: Double, q: String)
    val target = toLog4(Foursy)
    target.toLog(Foursy(42, y = true, 3.1415927, "x")) shouldBe "Foursy(x:42,y:true,z:3.1415927,q:x)"
  }

  it should "toLog4 with explicit field names" in {
    case class Foursy(x: Int, y: Boolean, z: Double, q: String)
    val target = toLog4(Foursy, Seq("x", "y", "z", "q"))
    target.toLog(Foursy(42, y = true, 3.1415927, "x")) shouldBe "Foursy(x:42,y:true,z:3.1415927,q:x)"
  }
}
