package com.phasmidsoftware.decisiontree.tree

import org.scalatest.matchers.should
import scala.collection.immutable.Queue

class TreeTest extends org.scalatest.flatspec.AnyFlatSpec with should.Matchers {

    behavior of "BFS"
    it should "bfs" in {
        val node1: Node[Int] = Node(1, Nil)
        val node2: Node[Int] = Node(2, Nil)
        val node0: Node[Int] = Node(0, Nil)
        val node4: Node[Int] = Node(4, Seq(node1, node2, node0))
        val list: Seq[Node[Int]] = List(node4)
        val target: Tree[Int] = Tree[Int](1, list)
        val z = target.bfs(_ => true)
        z shouldBe Queue(1, 4, 1, 2, 0)
    }

    behavior of "bfsPriorityQueue"
    it should "work" in {
        val node1: Node[Int] = Node(1, Nil)
        val node2: Node[Int] = Node(2, Nil)
        val node0: Node[Int] = Node(0, Nil)
        val node4: Node[Int] = Node(4, Seq(node1, node2, node0))
        val list: Seq[Node[Int]] = List(node4)
        val target: Tree[Int] = Tree[Int](1, list)
        val z: Iterable[Int] = Tree.bfsOrdered[Int](target, _ => true)
        z shouldBe List(0, 1, 1, 2, 4)
    }

}
