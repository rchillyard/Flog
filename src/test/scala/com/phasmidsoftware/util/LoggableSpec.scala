/*
 * Copyright (c) 2019. Phasmid Software
 */

package com.phasmidsoftware.util

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
}
