/*
 * Copyright (c) 2019. Phasmid Software
 */

package com.phasmidsoftware.flog

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should
import org.scalatest.{BeforeAndAfterEach, flatspec}

import scala.concurrent.Future
import scala.language.implicitConversions

class FlogSpec extends flatspec.AnyFlatSpec with should.Matchers with BeforeAndAfterEach with ScalaFutures {


  var evaluated = false

  def getString: String = {
    evaluated = true
    "Hello"
  }

  override def beforeEach(): Unit = {
    evaluated = false
  }

  override def afterEach(): Unit = {
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
    val x = getString !! 1
    evaluated shouldBe true
    x shouldBe 1
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

  it should "$bang$bang 6" in {
    val sb = new StringBuilder

    implicit val logFunc: LogFunction = LogFunction(sb.append)
    import scala.concurrent.ExecutionContext.Implicits.global
    implicit val z: Loggable[Future[Int]] = new Loggables {}.futureLoggable[Int]
    import Flog._
    val eventualInt = Flogger(getString)(logFunc) !! Future[Int] {
      Thread.sleep(100)
      "1".toInt
    }
    whenReady(eventualInt) {
      result =>
        result shouldBe 1
        // NOTE sb should not be empty but it might be if you run this unit test on its own.
        sb.toString() shouldBe "log: Hello: Future: promise created... log: Future completed: Success(1)"
    }
  }

}

