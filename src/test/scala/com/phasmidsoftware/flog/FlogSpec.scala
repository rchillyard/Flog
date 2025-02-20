/*
 * Copyright (c) 2021. Phasmid Software
 */

package com.phasmidsoftware.flog

import com.phasmidsoftware.flog.Loggable.loggableAny
import java.time.LocalDateTime
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should
import org.scalatest.{BeforeAndAfterEach, flatspec}
import scala.concurrent.Future
import scala.language.implicitConversions
import scala.util.control.NonFatal
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
    val flog: Flog = Flog(sb)
    import flog._
    getString !! 1
    if (!evaluated) println("evaluated should be true but it may not be if you run this unit test on its own")
    if (sb.toString != "log: Hello: 1") println("sb should not be empty but it will be if you run this unit test on its own")
  }

  it should "$bang$bang 1" in {
    val sb = new StringBuilder
    val flog: Flog = Flog(sb)
    import flog._
    getString !! List(1, 2, 3)
    if (!evaluated) println("evaluated should be true but it may not be if you run this unit test on its own")
    if (sb.toString.isEmpty) println("sb should not be empty but it will be if you run this unit test on its own")
    sb.toString shouldBe "Hello: [1, 2, 3]\n"
  }

  it should "$bang$bang 1a" in {
    val sb = new StringBuilder
    val flog: Flog = Flog(sb)
    import flog._
    val xs = getString !! List(1, 2, 3)
    xs shouldBe List(1, 2, 3)
    if (!evaluated) println("evaluated should be true but it may not be if you run this unit test on its own")
    if (sb.toString.isEmpty) println("sb should not be empty but it will be if you run this unit test on its own")
    sb.toString shouldBe "Hello: [1, 2, 3]\n"
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
  it should "$bang$bang Future[Int]" in {
    val sb: StringBuilder = new StringBuilder()
    implicit val sbLogger: Logger = Logger(sb) // TODO why is this not used?
    val flog = Flog(sb)
    import flog._
    import scala.concurrent.ExecutionContext.Implicits.global
    val eventualInt = getString !! Future[Int] {
      Thread.sleep(100)
      "1".toInt
    }
    whenReady(eventualInt) {
      result =>
        result shouldBe 1
        // NOTE sb should not be empty but it might be if you run this unit test on its own.
        val str = sb.toString().replaceAll("""\[\S+]""", "<UUID>")
        // NOTE occasionally, the completed message will precede the created message.
        println(str)
        val b1 = str == "Hello: future promise <UUID> created... \nHello: future <UUID> completed : Success(1)\n"
        val b2 = str == "Hello: future <UUID> completed : Success(1)\nHello: future promise <UUID> created... \n"
        val b3 = str == "Hello: future promise <UUID> created... \n"
        (b1 || b2 || b3) shouldBe true
    }
    flog.close()
  }

  it should "$bang$bang 7" in {
    // NOTE: check the log files to see if FlogSpec was the class of record.
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
    sb.toString shouldBe "Hello: <LazyList>\n"
  }

  it should "$bang$bang 10" in {
    val sb = new StringBuilder
    val flog = Flog(sb)
    import flog._
    getString !! List(1, 2, 3).view.map(_.toString)
    // NOTE sb should not be empty but it might be if you run this unit test on its own.
    sb.toString shouldBe "Hello: <view>\n"
  }

  it should "!! 10a" in {
    println("!! 14")
    val flog = Flog(classOf[FlogSpec])
    import flog._
    val iterator = Seq(1, 1, 2, 3, 5, 8).iterator
    getString !! iterator
  }

  it should "$bang$bang 11" in {
    val sb: StringBuilder = new StringBuilder
    val flog = Flog(sb)
    import flog._
    implicit val z: Loggable[Map[String, String]] = new Loggables {}.mapLoggable[String, String]() // XXX why not used?
    getString !! Map("a" -> "alpha", "b" -> "bravo")
    // NOTE sb should not be empty but it might be if you run this unit test on its own.
    sb.toString shouldBe "Hello: {a->alpha, b->bravo}\n"
  }

  it should "$bang$bang 12A using implict loggable for LocalDateTime" in {
    val sb: StringBuilder = new StringBuilder
    val flog = Flog(sb)
    import flog._
    getString !! Seq(LocalDateTime.now)
    val dateTimeR: Regex = """Hello: \[(\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}.\d{3,})]\n""".r
    // NOTE sb should not be empty but it might be if you run this unit test on its own.

    sb.toString match {
      case dateTimeR(_) =>
      case x => fail(s"incorrect response: $x")
    }
  }

  it should "$bang$bang 13" in {
    val flog: Flog = Flog[FlogSpec]
    import flog._
    implicit val loggableTryInt: Loggable[Try[Int]] = new Loggables{}.tryLoggable[Int]
    val result: Try[Int] = getString !! Try(1 / 0)
    result match {
      case Failure(NonFatal(e)) =>
        e.getClass shouldBe classOf[ArithmeticException]
        e.getLocalizedMessage shouldBe "/ by zero"
      case _ => fail("logic error")
    }
  }

  it should "$bang$bang 14 (alternative when using something for which there is no implicit Loggable)" in {
    case class Complex(r: Double, i: Double)
    val sb: StringBuilder = new StringBuilder
    val flog = Flog(sb)
    import flog._
    (getString !! List(Complex(0, 0)))(using loggableAny)

    sb.toString shouldBe
            """Hello: [Complex(0.0,0.0)]
              |""".stripMargin
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
    sb.toString shouldBe s"Hello: $now\n"
  }

  it should "handle unloggable class via LoggableAny" in {
    val sb = new StringBuilder
    val flog: Flog = Flog(sb)
    import flog._
    val now = LocalDateTime.now
    "Hello" !! now
    sb.toString shouldBe s"Hello: $now\n"
  }

  /**
   * NOTE you should see a full stack trace of the arithmetic exception in the log file.
   * NOTE if you want to avoid that stack trace, then don't use !!!
   */
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


  // NOTE: sometimes this test will fail. Not to worry.
  it should "debug 7" in {
    val sb: StringBuilder = new StringBuilder()
    implicit val logger: Logger = Logger(sb) // XXX why not used?
    val flog = Flog(sb)
    import flog._
    import scala.concurrent.ExecutionContext.Implicits.global
    val eventualInt = getString !? Future[Int] {
      Thread.sleep(100)
      "1".toInt
    }
    whenReady(eventualInt) {
      result =>
        result shouldBe 1
        // NOTE sb should not be empty but it might be if you run this unit test on its own.
        val str = sb.toString().replaceAll("""\[\S+]""", "<UUID>")
        // NOTE occasionally, the completed message will precede the created message.
        println(str)
        val b1 = str == "Hello: future promise <UUID> created... \nHello: future <UUID> completed : Success(1)\n"
        val b2 = str == "Hello: future <UUID> completed : Success(1)\nHello: future promise <UUID> created... \n"
        val b3 = str == "Hello: future promise <UUID> created... \n"
        (b1 || b2 || b3) shouldBe true
    }
    flog.close()
  }

  it should "trace 0" in {
    val flog = Flog[FlogSpec]
    import flog._
    val trace = flog.logger.isTraceEnabled
    if (trace) println("Trace enabled: expect to see log entry following")
    getString !?? 1
    evaluated shouldBe trace
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

  it should "trace 4" in {
    Using(Flog(System.out)) {
      f =>
        import f._
        getString !?? Seq(1, 2, 3)
        if (!evaluated) println("evaluated should be true but it may not be if you run this unit test on its own")
      // Should see message in console.
    }
  }

  it should "trace 5" in {
    Using(Flog(System.out)) {
      f =>
        import f._
        getString !?? Some(1)
        if (!evaluated) println("evaluated should be true but it may not be if you run this unit test on its own")
      // Should see message in console.
    }
  }

  it should "trace 6" in {
    Using(Flog(System.out)) {
      f =>
        import f._
        getString !?? Map(1 -> "a", 2 -> "b")
        if (!evaluated) println("evaluated should be true but it may not be if you run this unit test on its own")
      // Should see message in console.
    }
  }

  // NOTE: sometimes this test will fail. Not to worry.
  it should "trace 7" in {
    val sb: StringBuilder = new StringBuilder()
    implicit val logger: Logger = Logger(sb) // XXX why not used?
    val flog = Flog(sb)
    import flog._
    import scala.concurrent.ExecutionContext.Implicits.global
    val eventualInt = getString !?? Future[Int] {
      Thread.sleep(100)
      "1".toInt
    }
    whenReady(eventualInt) {
      result =>
        result shouldBe 1
        // NOTE sb should not be empty but it might be if you run this unit test on its own.
        val str = sb.toString().replaceAll("""\[\S+]""", "<UUID>")
        // NOTE occasionally, the completed message will precede the created message.
        println(str)
        val b1 = str == "Hello: future promise <UUID> created... \nHello: future <UUID> completed : Success(1)\n"
        val b2 = str == "Hello: future <UUID> completed : Success(1)\nHello: future promise <UUID> created... \n"
        val b3 = str == "Hello: future promise <UUID> created... \n"
        (b1 || b2 || b3) shouldBe true
    }
    flog.close()
  }

  it should "info 1" in {
    val flog = Flog[FlogSpec]
    import flog._
    getString info 1
    // The message "Hello: 1" should appear in the logs provided that info is enabled.
  }

  // This does indeed write to the logs but it shows DEBUG as level, not INFO.
  it should "info 2" in {
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

  it should "error 1" in {
    val logger: MockLogger = MockLogger(classOf[FlogSpec].toString, "ERROR")
    val flog: Flog = Flog(logger)
    import flog._
    getString.error(1)(new Exception("test"))
    logger.toString shouldBe "class com.phasmidsoftware.flog.FlogSpec: ERROR: Hello: 1 threw an exception: test\n"
    if (!evaluated) println("evaluated should be true but it may not be if you run this unit test on its own")
  }

  it should "error 2" in {
    val logger: MockLogger = MockLogger(classOf[FlogSpec].toString, "ERROR")
    val flog: Flog = Flog(logger)
    import flog._
    getString.error(1)()
    logger.toString shouldBe "class com.phasmidsoftware.flog.FlogSpec: ERROR: Hello: 1\n"
    if (!evaluated) println("evaluated should be true but it may not be if you run this unit test on its own")
  }

  it should "error 3" in {
    val sb = new StringBuilder()
    val flog: Flog = Flog(sb)
    import flog._
    getString.error(1)(new Exception("test"))
    flog.toString shouldBe "Hello: 1: ERROR: test\n"
    if (!evaluated) println("evaluated should be true but it may not be if you run this unit test on its own")
  }

  it should "error 4" in {
    val sb = new StringBuilder()
    val flog: Flog = Flog(sb)
    import flog._
    getString.error(1)()
    flog.toString shouldBe "Hello: 1: ERROR\n"
    if (!evaluated) println("evaluated should be true but it may not be if you run this unit test on its own")
  }

  it should "write to Appendable" in {
    Using(Flog(System.out)) {
      f =>
        import f._
        getString warn 1
    }
  }
}
