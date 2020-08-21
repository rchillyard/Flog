/*
 * Copyright (c) 2019. Phasmid Software
 */

package com.phasmidsoftware.util

import org.scalatest.{BeforeAndAfterEach, flatspec, matchers}
import scala.language.implicitConversions

class FlogSpec extends flatspec.AnyFlatSpec with matchers.should.Matchers with BeforeAndAfterEach {

  var evaluated = false

  def getString: String = {
    evaluated = true
    "Hello"
  }

  override def beforeEach(): Unit = {
    evaluated = false
  }

  override def afterEach() {
    Flog.enabled = true // we need to put the (singleton) value of enabled back the way it was.
    evaluated = false
  }

  behavior of "Flog"

  it should "$bang$bang 1" in {
    val sb = new StringBuilder

    import Flog._
    implicit val logFunc: LogFunction = LogFunction(sb.append)
    Flogger(getString)(logFunc) !! 1
    if (!evaluated) println("evaluated should be true but it will be if you run this unit test on its own")
    if (sb.toString != "log: Hello: 1") println("sb should not be empty but it will be if you run this unit test on its own")
  }

  it should "$bang$bang 2" in {
    val sb = new StringBuilder

    implicit val logFunc: LogFunction = LogFunction(_ => ())

    import Flog._
    Flogger(getString)(logFunc) !! 1
    evaluated shouldBe true
    sb.toString shouldBe ""
  }

  it should "$bang$bang 3" in {
    val sb = new StringBuilder

    Flog.enabled = false
    import Flog._
    getString !! 1
    evaluated shouldBe false
    sb.toString shouldBe ""
  }

  /**
   * In this test, we should see logging output according to the default value of Flog.loggingFunction
   */
  it should "$bang$bang 4" in {
    import Flog._
    getString !! 1
    evaluated shouldBe true
  }

  /**
   * In this test, we should see logging output according to the an explicit value of Flog.loggingFunction
   */
  it should "$bang$bang 5" in {
    Flog.loggingFunction = Flog.getLogger[FlogSpec]
    import Flog._
    getString !! 1
    evaluated shouldBe true
  }

  it should "$bar$bang" in {
    val sb = new StringBuilder
    import Flog._
    getString |! 1
    evaluated shouldBe false
    sb.toString shouldBe ""
  }

}

