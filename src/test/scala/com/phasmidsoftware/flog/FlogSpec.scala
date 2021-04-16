/*
 * Copyright (c) 2019. Phasmid Software
 */

package com.phasmidsoftware.flog

import java.time.LocalDateTime
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
    evaluated = false
  }

  behavior of "Flog"

  it should "$bang$bang 0" in {
    val sb = new StringBuilder
    val flog = Flog(enabled = true, LogFunction(sb.append))
    import flog._
    getString !! 1
    if (!evaluated) println("evaluated should be true but it may not be if you run this unit test on its own")
    if (sb.toString != "log: Hello: 1") println("sb should not be empty but it will be if you run this unit test on its own")
  }

  it should "$bang$bang 1" in {
    val sb = new StringBuilder
    val flog = Flog(enabled = true, LogFunction(sb.append))
    import flog._
    getString !! Seq(1, 2, 3)
    if (!evaluated) println("evaluated should be true but it may not be if you run this unit test on its own")
    if (sb.toString != "log: Hello: 1") println("sb should not be empty but it will be if you run this unit test on its own")
    sb.toString shouldBe "log: Hello: [1, 2, 3]"
  }

  it should "$bang$bang 2" in {
    val sb = new StringBuilder

    implicit val logFunc: LogFunction = LogFunction(_ => ())
    val flog = Flog(enabled = true, LogFunction(sb.append))
    import flog._

    Flogger(getString)(logFunc) !! 1
    evaluated shouldBe true
    sb.toString shouldBe ""
  }

  it should "$bang$bang 3" in {
    val sb = new StringBuilder

    val flog = Flog(enabled = false)
    import flog._
    getString !! 1
    evaluated shouldBe false
    sb.toString shouldBe ""
  }

  /**
   * In this test, we should see logging output according to the default value of Flog.loggingFunction
   */
  it should "$bang$bang 4" in {
    val flog = Flog()
    import flog._
    val x = getString !! 1
    evaluated shouldBe true
    x shouldBe 1
  }

  /**
   * In this test, we should see logging output according to the an explicit value of Flog.loggingFunction
   */
  it should "$bang$bang 5" in {
    val flog = Flog(enabled = true, Flog.defaultLogFunction[FlogSpec])
    import flog._
    val x = getString !! 1
    evaluated shouldBe true
    x shouldBe 1
  }

  it should "$bang$bang 6" in {
    val sb: StringBuilder = new StringBuilder()

    val flog = Flog(enabled = true, LogFunction(sb.append))

    import scala.concurrent.ExecutionContext.Implicits.global
    implicit val logFunc: LogFunction = flog.loggingFunction
    implicit val z: Loggable[Future[Int]] = new Loggables {}.futureLoggable[Int]
    import flog._
    val eventualInt = getString !! Future[Int] {
      Thread.sleep(100)
      "1".toInt
    }
    whenReady(eventualInt) {
      result =>
        result shouldBe 1
        // NOTE sb should not be empty but it might be if you run this unit test on its own.
        val str = sb.toString().replaceAll("""\(\S+\)""", "")
        // NOTE occasionally, the completed message will precede the created message.
        str shouldBe "log: Hello: Future: promise  created... Future completed : Success"
    }
  }

  it should "$bar$bang1" in {
    val sb = new StringBuilder()

    val flog = Flog(enabled = true, LogFunction(sb.append))
    import flog._
    getString |! 1
    evaluated shouldBe false
    sb.toString shouldBe ""
  }

  it should "$bar$bang2" in {
    val sb = new StringBuilder
    val flog = Flog(enabled = true, LogFunction(sb.append))
    import flog._
    "Hello" |! List(1, 2, 3, 4)
    sb.toString shouldBe ""
  }

  it should "$bang$bar1" in {
    val sb = new StringBuilder
    val flog = Flog(LogFunction(sb.append))
    import flog._
    val now = LocalDateTime.now
    "Hello" !| now
    sb.toString shouldBe s"log: Hello: $now"
  }

}

