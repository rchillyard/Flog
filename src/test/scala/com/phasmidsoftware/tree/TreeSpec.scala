package com.phasmidsoftware.tree

import com.phasmidsoftware.decisiontree.tree.{Goal, LazyTree, Tree}
import com.phasmidsoftware.util.{Flog, LogFunction, Loggable, Loggables}
import org.scalatest.flatspec
import org.scalatest.matchers.should

import scala.collection.immutable.Queue

class TreeSpec extends flatspec.AnyFlatSpec with should.Matchers {

  var evaluated = false

  def getString: String = {
    evaluated = true
    "Hello"
  }

  def beforeEach(): Unit = {
    evaluated = false
  }

  def afterEach(): Unit = {
    Flog.enabled = true // we need to put the (singleton) value of enabled back the way it was.
    evaluated = false
  }

  behavior of "preOrder"
  it should "work for small tree" in {
    val target = Tree(1, Seq(Tree(2, Seq(Tree(3)))))
    target.preOrder(_ => true) shouldBe Queue(1, 2, 3)
  }
  it should "work for larger tree" in {
    val target = Tree(1, Seq(Tree(2, Seq(Tree(3), Tree(4))), Tree(5, Seq(Tree(6), Tree(7)))))
    target.preOrder() shouldBe Queue(1, 2, 3, 4, 5, 6, 7)
  }
  it should "work when pruning away even branches" in {
    val target = Tree(1, Seq(Tree(2, Seq(Tree(3), Tree(4))), Tree(5, Seq(Tree(6), Tree(7)))))
    target.preOrder(x => x % 2 != 0) shouldBe Queue(1, 5, 7)
  }
  it should "work when pruning away odd branches" in {
    val target = Tree(1, Seq(Tree(2, Seq(Tree(3), Tree(4))), Tree(5, Seq(Tree(6), Tree(7)))))
    target.preOrder(x => x % 2 == 0) shouldBe Queue.empty
  }
  it should "work for a very deep tree" in {
    val n = 1000000
    val target: Tree[Int] = LazyList.from(1).take(n).foldLeft(Tree(0))((t, x) => Tree(x, Seq(t)))
    val preOrder = target.preOrder()
    preOrder.size shouldBe (n+1)
  }

  behavior of "inOrder"
  it should "work (1))" in {
    val target = Tree(1, Seq(Tree(2, Seq(Tree(3)))))
    target.inOrder() shouldBe Queue(3, 2, 1)
  }
  it should "work (2)" in {
    val target = Tree(1, Seq(Tree(2, Seq(Tree(3), Tree(4))), Tree(5, Seq(Tree(6), Tree(7)))))
    target.inOrder() shouldBe Queue(3, 2, 4, 1, 6, 5, 7)
  }
  it should "work when pruning away even branches" in {
    val target = Tree(1, Seq(Tree(2, Seq(Tree(3), Tree(4))), Tree(5, Seq(Tree(6), Tree(7)))))
    target.inOrder(x => x % 2 != 0) shouldBe Queue(1, 5, 7)
  }
  it should "work when pruning away odd branches" in {
    val target = Tree(1, Seq(Tree(2, Seq(Tree(3), Tree(4))), Tree(5, Seq(Tree(6), Tree(7)))))
    target.inOrder(x => x % 2 == 0) shouldBe Queue.empty
  }
  it should "work for a very deep tree" in {
    val n = 1000000
    val target: Tree[Int] = LazyList.from(1).take(n).foldLeft(Tree(0))((t, x) => Tree(x, Seq(t)))
    val inOrder = target.inOrder()
    inOrder.size shouldBe (n+1)
  }

  behavior of "postOrder"
  it should "work (1))" in {
    val target = Tree(1, Seq(Tree(2, Seq(Tree(3)))))
    target.postOrder() shouldBe Queue(3, 2, 1)
  }
  it should "work (2)" in {
    val target = Tree(45, Seq(Tree(25, Seq(Tree(15), Tree(35))), Tree(75)))
    target.postOrder() shouldBe Queue(15, 35, 25, 75, 45)
  }
  it should "work (3)" in {
    val target = Tree(1, Seq(Tree(2, Seq(Tree(4), Tree(5))), Tree(3, Seq(Tree(6), Tree(7)))))
    target.postOrder() shouldBe Queue(4, 5, 2, 6, 7, 3, 1)
  }
  it should "work when pruning away even branches" in {
    val target = Tree(1, Seq(Tree(2, Seq(Tree(3), Tree(4))), Tree(5, Seq(Tree(6), Tree(7)))))
    target.postOrder(x => x % 2 != 0) shouldBe Queue(7, 5, 1)
  }
  it should "work when pruning away odd branches" in {
    val target = Tree(1, Seq(Tree(2, Seq(Tree(3), Tree(4))), Tree(5, Seq(Tree(6), Tree(7)))))
    target.postOrder(x => x % 2 == 0) shouldBe Queue.empty
  }
  // FIXME why does this test not work? The tree builds OK but the postOrder never completes.
  ignore should "work for a very deep tree" in {
    val n = 1000000
    val target: Tree[Int] = LazyList.from(1).take(n).foldLeft(Tree(0))((t, x) => Tree(x, Seq(t)))
    val postOrder = target.postOrder()
    postOrder.size shouldBe (n + 1)
  }

  behavior of "BFS"
  it should "work (1))" in {
    val target = Tree(1, Seq(Tree(2, Seq(Tree(3)))))
    target.bfs() shouldBe Queue(1, 2, 3)
  }
  it should "work (2)" in {
    val target = Tree(45, Seq(Tree(25, Seq(Tree(15), Tree(35))), Tree(75)))
    target.bfs() shouldBe Queue(45, 25, 75, 15, 35)
  }
  it should "work (3)" in {
    val target = Tree(1, Seq(Tree(2, Seq(Tree(4), Tree(5))), Tree(3, Seq(Tree(6), Tree(7)))))
    target.bfs() shouldBe Queue(1, 2, 3, 4, 5, 6, 7)
  }
  it should "work when pruning away even branches" in {
    val target = Tree(1, Seq(Tree(2, Seq(Tree(3), Tree(4))), Tree(5, Seq(Tree(6), Tree(7)))))
    target.bfs(x => x % 2 != 0) shouldBe Queue(1, 5, 7)
  }
  it should "work when pruning away odd branches" in {
    val target = Tree(1, Seq(Tree(2, Seq(Tree(3), Tree(4))), Tree(5, Seq(Tree(6), Tree(7)))))
    target.bfs(x => x % 2 == 0) shouldBe Queue.empty
  }

  behavior of "ordered BFS"
  it should "work (1))" in {
    val target = Tree(1, Seq(Tree(2, Seq(Tree(3)))))
    implicit val goal: Goal[Int] = Goal.goal(t => t % 2 == 0)
    target.targetedBFS() shouldBe List(2)
  }
  it should "work (2)" in {
    val target = Tree(45, Seq(Tree(25, Seq(Tree(15), Tree(35))), Tree(75)))
    implicit val goal: Goal[Int] = Goal.goal(t => t % 2 == 0)
    target.targetedBFS() shouldBe Nil
  }
  it should "work (3)" in {
    val target = Tree(1, Seq(Tree(2, Seq(Tree(4), Tree(5))), Tree(3, Seq(Tree(6), Tree(7)))))
    implicit val goal: Goal[Int] = Goal.goal(t => t >= 5)
    target.targetedBFS() shouldBe List(3, 7)
  }
  it should "work when pruning away even branches" in {
    val target = Tree(1, Seq(Tree(2, Seq(Tree(3), Tree(4))), Tree(5, Seq(Tree(6), Tree(7)))))
    implicit val goal: Goal[Int] = Goal.goal(t => t % 2 != 0)
    target.targetedBFS() shouldBe List(1)
  }
  it should "work when pruning away odd branches" in {
    val target = Tree(1, Seq(Tree(2, Seq(Tree(3), Tree(4))), Tree(5, Seq(Tree(6), Tree(7)))))
    implicit val goal: Goal[Int] = Goal.goal(t => t % 2 == 0)
    target.targetedBFS() shouldBe List(5, 6)
  }
  it should "work for lazy nodes" in {
    val sb = new StringBuilder
    val loggables = new Loggables {}
    val generatorFunction: Int => Seq[Int] = t => {
      import Flog._
      import com.phasmidsoftware.util.Loggable._
      implicit val logFunc: LogFunction = LogFunction(s => sb.append(s).append("\n"))
      implicit val loggableSeqInt: Loggable[List[Int]] = loggables.listLoggable[Int]
      Flogger(getString)(logFunc) !! List(t * 2 + 1, t * 2 + 3)
    }
    implicit val goal: Goal[Int] = Goal.goal(t => t > 100 || t % 2 == 0)
    val target = LazyTree[Int](1)(generatorFunction)
    Tree.TreeOps(target).targetedBFS() shouldBe List(5, 13, 29, 61, 125)
    sb.toString() shouldBe "log: Hello: [3, 5]\nlog: Hello: [11, 13]\nlog: Hello: [27, 29]\nlog: Hello: [59, 61]\nlog: Hello: [123, 125]\n"
  }

  behavior of "map"
  it should "work" in {
    val target = Tree(1, Seq(Tree(2, Seq(Tree(3)))))
    target.map(_.toString) shouldBe Tree("1", Seq(Tree("2", Seq(Tree("3")))))
  }

  //  behavior of "flatMap"
  //  it should "work" in {
  //    val target = Tree(1, Seq(Tree(2, Seq(Tree(3)))))
  //    target.flatMap(Tree(_)) shouldBe Tree("1", Seq(Tree("2", Seq(Tree("3")))))
  //  }
}
