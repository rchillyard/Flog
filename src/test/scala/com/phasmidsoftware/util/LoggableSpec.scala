/*
 * Copyright (c) 2019. Phasmid Software
 */

package com.phasmidsoftware.util

import org.scalatest.{FlatSpec, Matchers}

//noinspection ScalaStyle
class LoggableSpec extends FlatSpec with Matchers {

  behavior of "Loggable"

  it should "toLog" in {
    implicitly[Loggable[Boolean]].toLog(true) shouldBe "true"
    implicitly[Loggable[Int]].toLog(42) shouldBe "42"
    implicitly[Loggable[Double]].toLog(42.0) shouldBe "42.0"
    implicitly[Loggable[String]].toLog("42") shouldBe "42"
  }
}
