/*
 * Copyright (c) 2019. Phasmid Software
 */

package com.phasmidsoftware.util

import org.scalatest.matchers.should
import org.scalatest.{BeforeAndAfterEach, flatspec}

class SmartValueOpsSpec extends flatspec.AnyFlatSpec with should.Matchers with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    SmartValueOps.setEnabledInvariants(true)
    SmartValueOps.setEnabledConsole(true)
  }

  import SmartValueOps._

  behavior of "invariant"

  it should "apply with StringBuilderOutputStream" in {
    val outputStream = new StringBuilderOutputStream()
    Console.withOut(outputStream)((Math.PI * 2).invariant(x => x > 0, "x should be positive")) shouldBe Math.PI * 2
    Console.withOut(outputStream)((Math.PI * 2).invariant(x => x < 0, "x should be negative")) shouldBe Math.PI * 2
    outputStream.toString() shouldBe "x should be negative: 6.283185307179586\n"
  }

  it should "apply with logging" in {
    val logger = MockLogger("SmartValue")
    (Math.PI * 2).invariant(x => x > 0, logger, "x should be positive") shouldBe Math.PI * 2
    (Math.PI * 2).invariant(x => x < 0, logger, "x should be negative") shouldBe Math.PI * 2
    logger.toString shouldBe "SmartValue: DEBUG: x should be negative: 6.283185307179586\n"
  }

  it should "apply with exception" in {
    (Math.PI * 2).invariant(x => x > 0) shouldBe Math.PI * 2
    an[Exception] shouldBe thrownBy((Math.PI * 2).invariant(x => x < 0))
  }

  behavior of "invariant turned off"

  it should "apply with StringBuilderOutputStream" in {
    SmartValueOps.setEnabledInvariants(false)
    val outputStream = new StringBuilderOutputStream()
    Console.withOut(outputStream)((Math.PI * 2).invariant(x => x > 0, "x should be positive")) shouldBe Math.PI * 2
    Console.withOut(outputStream)((Math.PI * 2).invariant(x => x < 0, "x should be negative")) shouldBe Math.PI * 2
    outputStream.toString() shouldBe ""
  }

  it should "apply with logging" in {
    SmartValueOps.setEnabledInvariants(false)
    val logger = MockLogger("SmartValue")
    (Math.PI * 2).invariant(x => x > 0, logger, "x should be positive") shouldBe Math.PI * 2
    (Math.PI * 2).invariant(x => x < 0, logger, "x should be negative") shouldBe Math.PI * 2
    logger.toString shouldBe ""
  }

  it should "apply with exception" in {
    SmartValueOps.setEnabledInvariants(false)
    (Math.PI * 2).invariant(x => x > 0) shouldBe Math.PI * 2
    (Math.PI * 2).invariant(x => x < 0) shouldBe Math.PI * 2
  }

  behavior of "console"

  it should "console" in {
    val outputStream = new StringBuilderOutputStream()
    Console.withOut(outputStream)((Math.PI * 2).console("the value of x is")) shouldBe Math.PI * 2
    outputStream.toString() shouldBe "the value of x is: 6.283185307179586\n"
  }

  it should "not console" in {
    SmartValueOps.setEnabledConsole(false)
    val outputStream = new StringBuilderOutputStream()
    Console.withOut(outputStream)((Math.PI * 2).console("the value of x is")) shouldBe Math.PI * 2
    outputStream.toString() shouldBe ""
  }

  behavior of "logging"

  it should "debug 1" in {
    implicit val logger: MockLogger = MockLogger("SmartValue")
    (Math.PI * 2).debug("the value of Pi is") shouldBe Math.PI * 2
    logger.toString shouldBe "SmartValue: DEBUG: the value of Pi is: 6.283185307179586\n"
  }

  it should "debug 2" in {
    implicit val logger: MockLogger = MockLogger("SmartValue")
    (Math.PI * 2).debug("{} is the value of Pi") shouldBe Math.PI * 2
    logger.toString shouldBe "SmartValue: DEBUG: 6.283185307179586 is the value of Pi\n"
  }

  it should "info" in {
    implicit val logger: MockLogger = MockLogger("SmartValue", "INFO")
    (Math.PI * 2).info("the value of Pi is") shouldBe Math.PI * 2
    logger.toString shouldBe "SmartValue: INFO: the value of Pi is: 6.283185307179586\n"
  }

  it should "warn" in {
    implicit val logger: MockLogger = MockLogger("SmartValue", "WARN")
    (Math.PI * 2).warn("the value of Pi is") shouldBe Math.PI * 2
    logger.toString shouldBe "SmartValue: WARN: the value of Pi is: 6.283185307179586\n"
  }

}
