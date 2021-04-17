/*
 * Copyright (c) 2019. Phasmid Software
 */

package com.phasmidsoftware.flog

import org.scalatest.flatspec
import org.scalatest.matchers.should

//noinspection ScalaStyle
class LoggableSpec extends flatspec.AnyFlatSpec with should.Matchers {

  behavior of "Loggable"

  it should "toLog" in {
    implicitly[Loggable[Boolean]].toLog(true) shouldBe "true"
    implicitly[Loggable[Int]].toLog(42) shouldBe "42"
    implicitly[Loggable[Double]].toLog(42.0) shouldBe "42.0"
    implicitly[Loggable[String]].toLog("42") shouldBe "42"
  }

  it should "work with import" in {
    // NOTE: these calls should write to the log file but there are no tests here.
    val flog = Flog()
    import flog._

    "Hello" !! () // log an instance of Unit
    "Hello" !! 1 // log an Int
    "Hello" !! 1L
    "Hello" !! "World!"
    "Hello" !! List(1, 2, 3, 4) // log an iterable
    "Hello" !! Option(1)
    "Hello" !! Option("World!")
    "Hello" !! 1.toByte
    "Hello" !! 1.toShort
    "Hello" !! 1.0
    "Hello" !! BigInt(1)
    "Hello" !! BigDecimal(1)
  }
}
