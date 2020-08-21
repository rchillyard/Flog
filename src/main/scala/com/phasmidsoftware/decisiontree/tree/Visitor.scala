package com.phasmidsoftware.decisiontree.tree

import com.phasmidsoftware.util.PriorityQueue
import scala.collection.immutable.Queue

/**
 * Type class to define a visitor for the nodes of a Tree.
 *
 * There is one method defined: visit.
 */
trait Visitor[T, V] {
  /**
   * Method to be called when visiting a Node[T] of a Tree[T].
   *
   * @param v the visitor, of type V.
   * @param t the key of the current Node.
   * @return a new visitor, based on v and t.
   */
  def visit(v: V, t: T): V
}

/**
 * Companion object of Visitor.
 */
object Visitor {

  /**
   * QueueVisitor.
   *
   * @tparam T the underlying type.
   */
  trait QueueVisitor[T] extends Visitor[T, Queue[T]] {
    def visit(tq: Queue[T], t: T): Queue[T] = tq.enqueue(t)
  }

  implicit def queueVisitor[T]: QueueVisitor[T] = new QueueVisitor[T] {}

  /**
   * PriorityQueueVisitor.
   *
   * @tparam T the underlying type.
   */
  trait PriorityQueueVisitor[T] extends Visitor[T, PriorityQueue[T]] {
    def visit(tq: PriorityQueue[T], t: T): PriorityQueue[T] = tq.insert(t)
  }

  implicit def priorityQueueVisitor[T]: PriorityQueueVisitor[T] = new PriorityQueueVisitor[T] {}

  /**
   * This is the equivalent of StackVisitor.
   * NOTE that Seq is a super-type of List.
   *
   * @tparam T the underlying type.
   */
  trait SeqVisitor[T] extends Visitor[T, Seq[T]] {
    def visit(ts: Seq[T], t: T): Seq[T] = t +: ts
  }

  implicit def seqVisitor[T]: SeqVisitor[T] = new SeqVisitor[T] {}
}
