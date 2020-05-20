package com.phasmidsoftware.decisiontree.tree

import scala.annotation.tailrec
import scala.collection.immutable.Queue

trait Monadic[T] {
//  def map[U](f: T => U): Node[U] = ???
//
//  def flatMap[U](f: T => Node[U]): Node[U] = ???

  def filter(p: T => Boolean): Boolean
}

trait Node[T] extends Monadic[T] {
  val key: T
  val children: Seq[Node[T]]

  def filter(p: T => Boolean): Boolean = p(key)
}

object Node {
  /**
   * Factory method to create a Tree[T].
   *
   * @param t   the key value of the root node.
   * @param tns a sequence of Node[T]s.
   * @tparam T the key type of the nodes.
   * @return a Tree[T].
   */
  def apply[T](t: T, tns: Seq[Node[T]]): Node[T] = Tree(t, tns)

  /**
   * Factory method to create a leaf node.
   *
   * @param t the key value of the root node.
   * @tparam T the key type of the nodes.
   * @return a Tree[T] which has no children.
   */
  def leaf[T](t: T): Node[T] = apply(t, Nil)
}

/**
 * Case class to define a Tree[T] which extends Node[T].
 *
 * @param key      the key value of the node.
 * @param children the children of the node.
 * @tparam T the underlying type of the key.
 */
case class Tree[T](key: T, children: Seq[Node[T]] = Nil) extends Node[T]

object Tree {

  /**
   * Implicit class for performing DFS traverses:
   * preOrder, inOrder, and postOrder.
   *
   * @param node a Node[T]
   * @tparam T the underlying key type.
   */
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
        case tn :: tns if tn.filter(p) => inner(queue.enqueue(tn.key), tn.children ++ tns)
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
            if (tn.filter(p))
              tn.children match {
                case Nil =>
                  inner(queue, Right(tn.key) +: ws)
                case first :: rest =>
                  inner(queue, Left(first) +: Right(tn.key) +: (rest.map(Left(_)) ++ ws))
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
            if (tn.filter(p))
              inner(queue, tn.children.map(Left(_)) ++ (Right(tn.key) +: ws))
            else
              inner(queue, ws)
          case Right(t) =>
            inner(queue.enqueue(t), ws)
        }
      }

      inner(Queue.empty, Seq(Left(node)))
    }

    def bfs(p: T => Boolean = always): Iterable[T] = {
      @tailrec
      def inner(queue1: Queue[T], queue2: Queue[Node[T]]): Queue[T] = {
        queue2.dequeueOption match {
          case None => queue1
          case Some((tn, q)) =>
            if (tn.filter(p))
              inner(queue1.enqueue(tn.key), tn.children.foldLeft(q)(_.enqueue(_)))
            else inner(queue1, q)
        }
      }

      inner(Queue.empty, Queue(node))
    }
  }

  private val always: Any => Boolean = _ => true
}
