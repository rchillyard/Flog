/*
 * Copyright (c) 2019. Phasmid Software
 */

package com.phasmidsoftware.util

import java.io.OutputStream
import org.scalatest.{BeforeAndAfterEach, flatspec, matchers}

class PipeSpec extends flatspec.AnyFlatSpec with matchers.should.Matchers
        with BeforeAndAfterEach {

  implicit val logger: MockLogger = MockLogger("PipeSpec", "Warn")

  override def beforeEach() {
    logger.clear()
  }

  override def afterEach() {
  }

  behavior of "Pipe"

  it should "apply 0" in {
    val target: Pipe[Int] = Pipe[Int]()
    target(1) shouldBe 1
    logger.toString shouldBe ""
  }

  it should "apply 1" in {
    val target = Pipe(tee = { x: Int => logger.warn(s"warning re: $x") })
    target(1) shouldBe 1
    logger.toString shouldBe "PipeSpec: Warn: warning re: 1\n"
  }

  it should "apply 2" in {
    val outputStream = new StringBuilderOutputStream()
    val target = Pipe(tee = PipeSpec.hello)
    Console.withOut(outputStream)(target(1)) shouldBe 1
    outputStream.toString shouldBe "Hello: 1\n"
  }

  it should "apply 3" in {
    val outputStream = new StringBuilderOutputStream()
    val target = Pipe(tee = PipeSpec.hello, predicate = PipeSpec.positive)
    Console.withOut(outputStream)(target(1)) shouldBe 1
    outputStream.toString shouldBe "Hello: 1\n"
  }

  it should "log an exception" in {
    val target = Pipe[Int](_ => throw new Exception("x"))
    target(1) shouldBe 1
    logger.toString shouldBe "PipeSpec: Warn: Pipe.apply(): an exception was thrown in either predicate or tee threw an exception: x\n"
  }

  it should "not" in {
    val outputStream = new StringBuilderOutputStream()
    val pipe = Pipe(tee = PipeSpec.hello, predicate = PipeSpec.positive).not
    Console.withOut(outputStream)(pipe(1)) shouldBe 1
    Console.withOut(outputStream)(pipe(-1)) shouldBe -1
    outputStream.toString shouldBe "Hello: -1\n"
  }

  it should "and" in {
    val outputStream = new StringBuilderOutputStream()
    val pipe = Pipe(tee = PipeSpec.hello, predicate = PipeSpec.positive).and(PipeSpec.isEven)
    Console.withOut(outputStream)(pipe(-1)) shouldBe -1
    Console.withOut(outputStream)(pipe(1)) shouldBe 1
    Console.withOut(outputStream)(pipe(-2)) shouldBe -2
    Console.withOut(outputStream)(pipe(2)) shouldBe 2
    outputStream.toString shouldBe "Hello: 2\n"
  }

  it should "or" in {
    val outputStream = new StringBuilderOutputStream()
    val pipe = Pipe(tee = PipeSpec.hello, predicate = PipeSpec.positive).or(PipeSpec.isEven)
    Console.withOut(outputStream)(pipe(-1)) shouldBe -1
    Console.withOut(outputStream)(pipe(1)) shouldBe 1
    Console.withOut(outputStream)(pipe(-2)) shouldBe -2
    Console.withOut(outputStream)(pipe(2)) shouldBe 2
    outputStream.toString shouldBe "Hello: 1\nHello: -2\nHello: 2\n"
  }

  it should "andThen" in {
    val outputStream = new StringBuilderOutputStream()
    Console.withOut(outputStream)(Pipe(tee = { x: Int => println(s"Hello: $x") }).andThen(_ * 2)(1)) shouldBe 2
    outputStream.toString shouldBe "Hello: 1\n"
  }

  it should "compose" in {
    val outputStream = new StringBuilderOutputStream()
    Console.withOut(outputStream)(Pipe[Int](tee = { x: Int => println(s"Hello: $x") }).compose { x: Int => x * 2 }(1)) shouldBe 2
    outputStream.toString shouldBe "Hello: 2\n"
  }

}

object PipeSpec {
  private def hello(x: Int): Unit = println(s"Hello: $x")

  private def positive(x: Int): Boolean = x > 0

  private def isEven(x: Int): Boolean = x % 2 == 0
}

class StringBuilderOutputStream() extends OutputStream {
  val sb = new StringBuilder()

  def write(b: Int): Unit = sb.append(b.toChar)

  override def toString: String = sb.toString
}