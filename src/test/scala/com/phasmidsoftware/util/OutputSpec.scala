/*
 * Copyright (c) 2019. Phasmid Software
 */

package com.phasmidsoftware.util

import java.io.Writer
import org.scalatest.{flatspec, matchers}

import org.scalatest.flatspec
import org.scalatest.matchers.should

class OutputSpec extends flatspec.AnyFlatSpec with should.Matchers {

  behavior of "trait Output"

  it should "insertBreak" in {
    val target: BufferedOutput = Output.empty.insertBreak().asInstanceOf[BufferedOutput]
    target.content shouldBe "\n"
  }

  it should "insertBreak after indent" in {
    val output = Output.empty.indent("    ")
    output.asInstanceOf[UnbackedOutput].indentation shouldBe "    "
    val target: BufferedOutput = output.insertBreak().asInstanceOf[BufferedOutput]
    target.content shouldBe "\n    "
    target.asInstanceOf[UnbackedOutput].indentation shouldBe "    "
  }

  it should "indent" in {
    val output = Output.empty.indent("    ")
    output.asInstanceOf[UnbackedOutput].indentation shouldBe "    "
    val result = output.indent("    ")
    result.asInstanceOf[UnbackedOutput].indentation shouldBe "        "
    val target: BufferedOutput = result.insertBreak().asInstanceOf[BufferedOutput]
    target.asInstanceOf[UnbackedOutput].indentation shouldBe "        "
    target.content shouldBe "\n        "
  }

  it should "indent with ++" in {
    val writer1 = MockWriter()
    val output1 = Output(writer1).indent("    ")
    val writer2 = MockWriter()
    val output2 = Output(writer2)
    val result = output1 ++ output2
    result.asInstanceOf[WriterOutput].indentation shouldBe "    "
  }

  it should ":+" in {
    val writer = MockWriter()
    val output = Output(writer) :+ "x"
    output match {
      case w@WriterOutput(_, _, _) => w.sb.toString() shouldBe "x"
      case _ => fail("bad")
    }
  }

  it should "+:" in {
    val writer = MockWriter()
    val output = "x" +: Output(writer)
    output match {
      case b: BufferedOutput => b.content shouldBe "x"
      case _ => fail("bad")
    }
  }

  it should "close" in {
    val writer = MockWriter()
    val output = Output(writer) :+ "x"
    output.close()
    // TODO ideally, output.close should close the writer, too--but that doesn't happen.
    //		writer.isOpen shouldBe false
    writer.total shouldBe 1
    writer.spillway shouldBe "x"
  }

  it should "WriterOutput ++ WriterOutput" in {
    val writerX = MockWriter()
    val writerY = MockWriter()
    val x: Output = Output(writerX) :+ "x"
    val y: Output = Output(writerY) :+ "y"
    val output: BufferedCharSequenceOutput[Writer] = (x ++ y).asInstanceOf[BufferedCharSequenceOutput[Writer]]
    writerX.total shouldBe 1
    writerX.spillway shouldBe "x"
    writerY.total shouldBe 0
    writerY.spillway shouldBe ""
    output.sb.toString shouldBe "y"
    output.close()
    writerY.total shouldBe 1
    writerY.spillway shouldBe "y"
  }

  it should "WriterOutput ++ UnbackedOutput" in {
    val writerX = MockWriter()
    val x: Output = Output(writerX) :+ "x"
    val y: Output = UnbackedOutput() :+ "y"
    val output: BufferedCharSequenceOutput[Writer] = (x ++ y).asInstanceOf[BufferedCharSequenceOutput[Writer]]
    writerX.total shouldBe 0
    // CONSIDER restoring this
    //		output.sb.toString shouldBe "xy"
    output.close()
    writerX.total shouldBe 2
    writerX.spillway shouldBe "xy"
  }

  it should "UnbackedOutput ++ WriterOutput" in {
    val x: Output = UnbackedOutput() :+ "x"
    val writerY = MockWriter()
    val y: Output = Output(writerY) :+ "y"
    val output = x ++ y
    val writerZ = MockWriter()
    (WriterOutput(writerZ) ++ output).close()
    // CONSIDER restoring this
    //		writerZ.total shouldBe 2
    writerZ.spillway shouldBe "xy"
  }

  it should "UnbackedOutput ++ flushed WriterOutput" in {
    val x: Output = Output("x").asInstanceOf[BufferedOutput]
    val writerY = MockWriter()
    val y: Output = (Output(writerY) :+ "y").asInstanceOf[BufferedOutput].flush
    an[OutputException] should be thrownBy x ++ y
  }

  it should "UnbackedOutput ++ UnbackedOutput" in {
    val x: Output = UnbackedOutput() :+ "x"
    val y: Output = UnbackedOutput() :+ "y"
    val output: BufferedCharSequenceOutput[Writer] = (x ++ y).asInstanceOf[BufferedCharSequenceOutput[Writer]]
    output.sb.toString shouldBe "xy"
    an[OutputException] should be thrownBy output.close()
  }

  it should "do multiple ++ with close" in {
    val x1 = Output("x1")
    val x2 = Output("x2")
    val x3 = Output("x3")
    val writer = MockWriter()
    val output = Output(writer) ++ x1 ++ x2 ++ x3
    output.close()
    writer.spillway shouldBe "x1x2x3"
  }

  it should "++(Iterator)" in {
    val writerX = MockWriter()
    val x: Output = Output(writerX) :+ "x"
    val y: Output = UnbackedOutput() :+ "y"
    val output: BufferedCharSequenceOutput[Writer] = (x ++ y).asInstanceOf[BufferedCharSequenceOutput[Writer]]
    writerX.total shouldBe 0
    output.sb.toString shouldBe "xy"
    output.close()
    writerX.total shouldBe 2
    writerX.spillway shouldBe "xy"
  }

  behavior of "object Output"

  it should "apply1" in {
    val target = Output.empty
    target.getClass shouldBe classOf[UnbackedOutput]
  }

  it should "apply2" in {
    val target = Output.apply("x")
    target.getClass shouldBe classOf[UnbackedOutput]
    target.asInstanceOf[BufferedOutput].content shouldBe "x"
  }

  it should "reduce" in {

  }

  it should "sum" in {

  }

  behavior of "TypedOutput"

  it should "zero" in {
    val z: Output = Output.empty
    z.asInstanceOf[TypedOutput].zero shouldBe ""
  }

  it should "unit" in {

  }

  it should "asOutputType" in {

  }

  it should "append" in {

  }

  behavior of "BufferedCharSequenceOutput"

  it should "flush" in {
    val writer = MockWriter()
    val output: BufferedCharSequenceOutput[Writer] = Output(writer, "x").asInstanceOf[BufferedCharSequenceOutput[Writer]]
    writer.total shouldBe 0
    val flushed: BufferedCharSequenceOutput[Writer] = output.flush.asInstanceOf[BufferedCharSequenceOutput[Writer]]
    flushed.sb.isEmpty shouldBe true
    writer.total shouldBe 1
    writer.isOpen shouldBe true
    writer.flush()
    writer.spillway shouldBe "x"
  }

  it should "not flush UnbackedOutput" in {
    an[OutputException] should be thrownBy (UnbackedOutput() :+ "x").asInstanceOf[BufferedOutput].flush
  }

  it should "maybeFlush" in {

  }

  it should "isBacked" in {

  }

  it should "bufferAsOutputType" in {

  }

}

case class MockWriter(n: Int = 4096, var isOpen: Boolean = true) extends Writer {
  var length = 0
  var spilled = 0
  val chars: Array[Char] = new Array[Char](n)
  var spillway = ""

  def content: String = chars.mkString.substring(0, length)

  override def toString: String = s"""MockWriter: isOpen=$isOpen, length=$length, spilled=$spilled and content="$content""""

  def spill(len: Int): String = {
    val toSpill = math.min(length, length + len - n)
    if (toSpill > 0) {
      val result = chars.take(toSpill).mkString("")
      if (toSpill + length <= n)
        Array.copy(chars, toSpill, chars, 0, length)
      else
        throw OutputException(s"logic error: buffer too small: $n but needs to be at least ${toSpill + length}")
      spilled += toSpill
      length -= toSpill
      result
    }
    else
      ""
  }

  def write(cbuf: Array[Char], off: Int, len: Int): Unit =
    if (isOpen) {
      spillway = spill(len)
      if (len + length <= n) {
        Array.copy(cbuf, off, chars, length, len)
        length += len
      }
      else throw OutputException(s"MockWriter: buffer too small: $n but should be ${len + length}")
    }
    else throw new Exception(s"MockWriter is closed")

  def flush(): Unit = {
    spillway = spill(n)
  }

  def close(): Unit = {
    flush()
    isOpen = false
  }

  def total: Int = length + spilled
}