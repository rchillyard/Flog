package com.phasmidsoftware.decisiontree.tree

import scala.annotation.tailrec
import scala.collection.immutable.Queue

trait Node[T] {
  val value: T
  val children: Seq[Node[T]]
}

case class Tree[T](value: T, children: Seq[Node[T]] = Nil) extends Node[T]

object Tree {

  implicit class TreeOps[T](node: Node[T]) {

    type Item = Either[Node[T], T]

    /**
     * Method to get the "preOrder" of a tree.
     * In preOrder, any node will have its value recorded BEFORE processing its children.
     *
     * @param p a predicate that determines whether a subtree with its value t will be processed at all.
     * @return an Iterable of T values in the order they were visited.
     */
    def preOrder(p: T => Boolean = always): Iterable[T] = {
      @tailrec
      def inner(queue: Queue[T], nodes: Seq[Node[T]]): Queue[T] = nodes match {
        case Nil => queue
        case tn :: tns if p(tn.value) => inner(queue.enqueue(tn.value), tn.children ++ tns)
        case _ :: tns => inner(queue, tns)
      }

      inner(Queue.empty, Seq(node))
    }

    /**
     * Method to get the "inOrder" of a tree.
     * In inOrder, any node will have its value recorded AFTER processing its first child,
     * but BEFORE processing the rest of its children.
     *
     * @param p a predicate that determines whether a subtree with its value t will be processed at all.
     * @return an Iterable of T values in the order they were visited.
     */
    def inOrder(p: T => Boolean = always): Iterable[T] = {
      @tailrec
      def inner(queue: Queue[T], work: Seq[Item]): Queue[T] = work match {
        case Nil => queue
        case w :: ws => w match {
          case Left(tn) =>
            if (p(tn.value))
              tn.children match {
                case Nil =>
                  inner(queue, Right(tn.value) +: ws)
                case first :: rest =>
                  inner(queue, Left(first) +: Right(tn.value) +: (rest.map(Left(_)) ++ ws))
              }
            else inner(queue, ws)
          case Right(t) =>
            inner(queue.enqueue(t), ws)
        }
      }

      inner(Queue.empty, Seq(Left(node)))
    }

    /**
     * Method to get the "postOrder" of a tree.
     * In postOrder, any node will have its value recorded AFTER processing all children,
     *
     * @param p a predicate that determines whether a subtree with its value t will be processed at all.
     * @return an Iterable of T values in the proper order.
     */
    def postOrder(p: T => Boolean = always): Iterable[T] = {
      @tailrec
      def inner(queue: Queue[T], work: Seq[Item]): Queue[T] = work match {
        case Nil => queue
        case w :: ws => w match {
          case Left(tn) =>
            if (p(tn.value))
              inner(queue, tn.children.map(Left(_)) ++ (Right(tn.value) +: ws))
            else
              inner(queue, ws)
          case Right(t) =>
            inner(queue.enqueue(t), ws)
        }
      }

      inner(Queue.empty, Seq(Left(node)))
    }

  }

  private def always(t: Any): Boolean = true
}
