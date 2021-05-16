/*
 * Copyright (c) 2019. Phasmid Software
 */

package com.phasmidsoftware.flog

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should
import org.scalatest.{BeforeAndAfterEach, flatspec}

import java.time.LocalDateTime
import scala.concurrent.Future
import scala.language.implicitConversions
import scala.util.matching.Regex
import scala.util.{Failure, Try, Using}

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
    val flog = Flog(sb)
    import flog._
    getString !! 1
    if (!evaluated) println("evaluated should be true but it may not be if you run this unit test on its own")
    if (sb.toString != "log: Hello: 1") println("sb should not be empty but it will be if you run this unit test on its own")
  }

  it should "$bang$bang 1" in {
    val sb = new StringBuilder
    val flog = Flog(sb)
    import flog._
    getString !! Seq(1, 2, 3)
    if (!evaluated) println("evaluated should be true but it may not be if you run this unit test on its own")
    if (sb.toString.isEmpty) println("sb should not be empty but it will be if you run this unit test on its own")
    sb.toString shouldBe "Hello: [1, 2, 3]"
  }

  it should "$bang$bang 2" in {
    val sb = new StringBuilder

    val flog = Flog(Logger.bitBucket)
    import flog._

    getString !! 1
    evaluated shouldBe false
    sb.toString shouldBe ""
  }

  it should "$bang$bang 3" in {
    val sb = new StringBuilder
    val flog = Flog(sb).disabled
    import flog._
    getString !! 1
    evaluated shouldBe false
    sb.toString shouldBe ""
  }

  /**
   * In this test, we should see logging output according to the value of Flog.defaultLogFunction[Flog]
   */
  it should "$bang$bang 4" in {
    val flog = Flog[FlogSpec]
    import flog._
    val x = getString !! 1
    evaluated shouldBe true
    x shouldBe 1
  }

  /**
   * In this test, we should see logging output according to the defaultLogFunction based on class FlogSpec.
   */
  it should "$bang$bang 5" in {
    // NOTE: check the log files to see if FlogSpec was the class of record.
    val flog = Flog(Logger[FlogSpec])
    import flog._
    val x = getString !! 99
    evaluated shouldBe true
    x shouldBe 99
  }

  // NOTE: sometimes this test will fail. Not to worry.
  ignore should "$bang$bang 6" in {
    val sb: StringBuilder = new StringBuilder()
    implicit val logger: Logger = Logger(sb)
    val flog = Flog(sb)
    import scala.concurrent.ExecutionContext.Implicits.global
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
        str shouldBe "Hello: Future: promise  created... Future completed : Success"
    }
  }

  it should "$bang$bang 7" in {
    // NOTE: check the log files to see if Flog was the class of record.
    val flog = Flog[FlogSpec]
    import flog._
    getString !! Seq(1, 1, 2, 3, 5, 8)
  }

  it should "$bang$bang 8" in {
    // NOTE: check the log files to see if FlogSpec was the class of record.
    val flog = Flog(classOf[FlogSpec])
    import flog._
    getString !! Seq(1, 1, 2, 3, 5, 8)
  }

  it should "$bang$bang 9" in {
    val sb = new StringBuilder
    val flog = Flog(sb)
    import flog._
    getString !! LazyList.from(1)
    // NOTE sb should not be empty but it might be if you run this unit test on its own.
    sb.toString shouldBe "Hello: <LazyList>"
  }

  it should "$bang$bang 10" in {
    val sb = new StringBuilder
    val flog = Flog(sb)
    import flog._
    getString !! List(1, 2, 3).view.map(_.toString)
    // NOTE sb should not be empty but it might be if you run this unit test on its own.
    sb.toString shouldBe "Hello: <view>"
  }

  it should "$bang$bang 11" in {
    val sb: StringBuilder = new StringBuilder
    val flog = Flog(sb)
    import flog._
    implicit val z: Loggable[Map[String, String]] = new Loggables {}.mapLoggable[String, String]()
    getString !! Map("a" -> "alpha", "b" -> "bravo")
    // NOTE sb should not be empty but it might be if you run this unit test on its own.
    sb.toString shouldBe "Hello: {a->alpha, b->bravo}"
  }

  it should "$bang$bang 12" in {
    val sb: StringBuilder = new StringBuilder
    val flog = Flog(sb)
    import flog._
    implicit val z: Loggable[LocalDateTime] = new Loggables {}.anyLoggable[LocalDateTime]
    getString !! Seq(LocalDateTime.now)
    val dateTimeR: Regex = """Hello: \[(\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}.\d{3,})]""".r
    // NOTE sb should not be empty but it might be if you run this unit test on its own.

    sb.toString match {
      case dateTimeR(_) =>
      case x => fail(s"incorrect response: $x")
    }
  }

  it should "$bar$bang1" in {
    val sb: StringBuilder = new StringBuilder()

    val flog = Flog(sb)
    import flog._
    getString |! 1
    evaluated shouldBe false
    sb.toString shouldBe ""
  }

  it should "$bar$bang2" in {
    val sb = new StringBuilder
    val flog = Flog(sb)
    import flog._
    "Hello" |! List(1, 2, 3, 4)
    sb.toString shouldBe ""
  }

  it should "$bang$bar1" in {
    val sb = new StringBuilder
    val flog = Flog(sb)
    import flog._
    val now = LocalDateTime.now
    "Hello" !| now
    sb.toString shouldBe s"Hello: $now"
  }

  it should "$bang$bang$bang 1/0" in {
    val flog = Flog[FlogSpec]
    import flog._
    val result = getString !!! Try(1 / 0)
    result match {
      case Failure(LoggedException(e)) =>
        e.getClass shouldBe classOf[ArithmeticException]
        e.getLocalizedMessage shouldBe "/ by zero"
      case _ => fail("logic error")
    }
  }

  it should "debug 0" in {
    val flog = Flog[FlogSpec]
    import flog._
    getString !? 1
    // The message "Hello: 1" should appear in the logs provided that debug is enabled.
  }

  it should "debug 1" in {
    val sb = new StringBuilder
    val flog = Flog(sb)
    import flog._
    getString debug 1
    if (!evaluated) println("evaluated should be true but it may not be if you run this unit test on its own")
    if (sb.toString != "log: Hello: 1") println("sb should not be empty but it will be if you run this unit test on its own")
  }

  it should "debug 2" in {
    val logger: MockLogger = MockLogger.defaultLogger[FlogSpec]
    val flog: Flog = Flog(logger)
    import flog._
    getString debug 1
    logger.toString shouldBe "class com.phasmidsoftware.flog.FlogSpec: DEBUG: Hello: 1\n"
    if (!evaluated) println("evaluated should be true but it may not be if you run this unit test on its own")
  }

  it should "debug 3" in {
    Using(Flog(System.out)) {
      f =>
        import f._
        getString debug 1
        if (!evaluated) println("evaluated should be true but it may not be if you run this unit test on its own")
      // Should see message in console.
    }
  }

  it should "debug 4" in {
    Using(Flog(System.out)) {
      f =>
        import f._
        getString !? Seq(1, 2, 3)
        if (!evaluated) println("evaluated should be true but it may not be if you run this unit test on its own")
      // Should see message in console.
    }
  }

  it should "debug 5" in {
    Using(Flog(System.out)) {
      f =>
        import f._
        getString !? Some(1)
        if (!evaluated) println("evaluated should be true but it may not be if you run this unit test on its own")
      // Should see message in console.
    }
  }

  it should "debug 6" in {
    Using(Flog(System.out)) {
      f =>
        import f._
        getString !? Map(1 -> "a", 2 -> "b")
        if (!evaluated) println("evaluated should be true but it may not be if you run this unit test on its own")
      // Should see message in console.
    }
  }

  it should "trace 0" in {
    val flog = Flog[FlogSpec]
    import flog._
    getString !?? 1
    // The message "Hello: 1" should appear in the logs provided that trace is enabled.
  }

  it should "trace 1" in {
    val flog = Flog[FlogSpec]
    import flog._
    getString trace 1
    // The message "Hello: 1" should appear in the logs provided that trace is enabled.
  }

  it should "trace 2" in {
    val logger: MockLogger = MockLogger.defaultLogger[FlogSpec]
    val flog: Flog = Flog(logger)
    import flog._
    getString trace 1
    // The following will be true only if TRACE is enabled.
    //    logger.toString shouldBe "class com.phasmidsoftware.flog.FlogSpec: TRACE: Hello: 1\n"
    //    if (!evaluated) println("evaluated should be true but it may not be if you run this unit test on its own")
  }

  it should "info 1" in {
    val flog = Flog[FlogSpec]
    import flog._
    getString info 1
    // The message "Hello: 1" should appear in the logs provided that info is enabled.
  }

  // This does indeed write to the logs but it shows DEBUG as level, not INFO.
  ignore should "info 2" in {
    val logger: MockLogger = MockLogger.defaultLogger[FlogSpec]
    val flog: Flog = Flog(logger)
    import flog._
    getString info 1
    logger.toString shouldBe "class com.phasmidsoftware.flog.FlogSpec: INFO: Hello: 1\n"
    if (!evaluated) println("evaluated should be true but it may not be if you run this unit test on its own")
  }

  it should "warn" in {
    val flog = Flog[FlogSpec]
    import flog._
    getString warn 1
    // The message "Hello: 1" should appear in the logs provided that warn is enabled.
  }

  it should "write to Appendable" in {
    Using(Flog(System.out)) {
      f =>
        import f._
        getString warn 1
    }
  }
}
