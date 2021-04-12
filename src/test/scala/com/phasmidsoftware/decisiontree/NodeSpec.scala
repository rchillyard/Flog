/*
 * Copyright (c) 2019. Phasmid Software
 */

package com.phasmidsoftware.decisiontree

import com.phasmidsoftware.util.{MockWriter, Output}
import org.scalatest.flatspec
import org.scalatest.matchers.should

//noinspection ScalaStyle
class NodeSpec extends flatspec.AnyFlatSpec with should.Matchers {

  behavior of "Node"

  it should "unit 1" in {
    val target = MockNode(1)
    val result = target.unit(2)
    result.t shouldBe 2
    result.children shouldBe Nil
  }

  it should "unit 2" in {
    val target = MockNode(1)
    val result = target.unit(2, List(MockNode(3)))
    result.t shouldBe 2
    result.children shouldBe List(MockNode(3))
  }

  it should "output 1" in {
    val target = MockNode(1)
    val writer = MockWriter()
    target.output(Output(writer)).close()
    writer.spillway shouldBe "1"
  }

  it should "output 2" in {
    val target = MockNode(1, List(MockNode(2, List(MockNode(3)))))
    val writer = MockWriter()
    target.output(Output(writer)).close()
    // TODO fix this -- there should be no space before the newlines
    writer.spillway shouldBe "1 \n  2 \n    3"
  }

  it should "output 3" in {
    val target = MockNode(1, List(MockNode(2, List(MockNode(3, List(MockNode(4)))))))
    val writer = MockWriter()
    target.output(Output(writer)).close()
    // TODO fix this -- there should be no space before the newlines
    writer.spillway shouldBe "1 \n  2 \n    3 \n      4"
  }

  it should "replace 1" in {
    val two = MockNode(2)
    val target = MockNode(1, List(two))
    val result: Node[Int] = target.replace(two, MockNode(3))
    result should matchPattern {
      case MockNode(1, None, List(MockNode(3, None, Nil))) =>
    }
  }

  it should "replace 2" in {
    val three = MockNode(3)
    val two = MockNode(2, List(three))
    val target = MockNode(1, List(two))
    val result: Node[Int] = target.replace(three, MockNode(4))
    result should matchPattern {
      case MockNode(1, None, List(MockNode(2, _, List(MockNode(4, _, Nil))))) =>
    }
  }

  it should "append 1" in {
    val two = MockNode(2)
    val target = MockNode(1, List(two))
    val result: Node[Int] = target.append(two, MockNode(3))
    result should matchPattern {
      case MockNode(1, None, List(MockNode(2, None, List(MockNode(3, None, Nil))))) =>
    }
  }

  it should "append 2" in {
    val three = MockNode(3)
    val two = MockNode(2, List(three))
    val target = MockNode(1, List(two))
    val result: Node[Int] = target.append(three, MockNode(4))
    result should matchPattern {
      case MockNode(1, None, List(MockNode(2, None, List(MockNode(3, None, List(MockNode(4, None, Nil))))))) =>
    }
  }

  it should "apply 3" in {
    val two = MockNode(2)
    val target = MockNode(1, List(two))
    val result: Node[Int] = target.append(two, 3)
    result should matchPattern {
      case MockNode(1, None, List(MockNode(2, None, List(MockNode(3, None, Nil))))) =>
    }
  }

  it should "append 4" in {
    val three = MockNode(3)
    val two = MockNode(2, List(three))
    val target = MockNode(1, List(two))
    val result: Node[Int] = target.append(three, 4)
    result should matchPattern {
      case MockNode(1, None, List(MockNode(2, None, List(MockNode(3, None, List(MockNode(4, None, Nil))))))) =>
    }
  }

  it should "unapply" in {
    MockNode.unapply(MockNode(1, Nil)) should matchPattern { case Some((1, None, Nil)) => }
    MockNode.unapply(MockNode(1, List(MockNode(2)))) should matchPattern { case Some((1, None, List(MockNode(2, None, Nil)))) => }
  }

  it should "depthFirstTraverse" in {
    val three = MockNode(3)
    val two = MockNode(2, List(three))
    val target = MockNode(1, List(two))
    val result = target.depthFirstTraverse
    result shouldBe List(3, 2, 1)
  }


}