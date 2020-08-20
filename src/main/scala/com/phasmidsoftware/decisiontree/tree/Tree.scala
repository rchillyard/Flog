package com.phasmidsoftware.decisiontree.tree

import com.phasmidsoftware.decisiontree.tree.Tree.TreeOps
import com.phasmidsoftware.decisiontree.tree.Visitor.QueueVisitor

import scala.annotation.tailrec
import scala.collection.immutable.Queue

trait Monadic[T] {
  def map[U](f: T => U): Monadic[U]

//  def flatMap[U](f: T => Monadic[U]): Monadic[U]

  def filter(p: T => Boolean): Boolean
}

trait Node[T] extends Monadic[T] {
  /**
   * The key of this Node.
   */
  val key: T

  /**
   * The children of this Node.
   */
  val children: Seq[Node[T]]

  /**
   * A method to filter this Node, based on its key.
   * @param p a Predicate on the key.
   * @return a Boolean.
   */
  def filter(p: T => Boolean): Boolean = p(key)

  /**
   * A method to transform this Node and its children (deep), according to the function f.
   *
   * @param f a function of T => U.
   * @tparam U the underlying type of the result.
   * @return a Node[V].
   */
  def map[U](f: T => U): Node[U] = new TreeOps(this).doMap(f)

//  override def flatMap[U](f: T => Monadic[U]): Node[U] = new TreeOps(this).doFlatMap(f)
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

    /**
     * Method to get the "preOrder" of a tree via depth-first search.
     * In preOrder, any node will have its value recorded BEFORE processing its children.
     *
     * @param p a predicate that determines whether a subtree with its value t will be processed at all.
     * @return an Iterable of T values in the order they were visited.
     */
    def preOrder(p: T => Boolean = always): Iterable[T] = traversePre(Queue.empty[T], p, Seq(node))

    /**
     * Method to get the "inOrder" of a tree via depth-first search.
     * In inOrder, any node will have its value recorded AFTER processing its first child,
     * but BEFORE processing the rest of its children.
     *
     * @param p a predicate that determines whether a subtree with its value t will be processed at all.
     * @return an Iterable of T values in the order they were visited.
     */
    def inOrder(p: T => Boolean = always): Iterable[T] = traverse(Queue.empty[T], p, inOrderFunc)(Seq(Left(node)))

    /**
     * Method to get the "postOrder" of a tree via depth-first search.
     * In postOrder, any node will have its value recorded AFTER processing all children,
     *
     * @param p a predicate that determines whether a subtree with its value t will be processed at all.
     * @return an Iterable of T values in the proper order.
     */
    def postOrder(p: T => Boolean = always): Iterable[T] = traverse(Queue.empty[T], p, postOrderFunc)(Seq(Left(node)))

    /**
     * Method to get the breadth-first order of a tree.
     *
     * @param p a predicate that determines whether a subtree with its value t will be processed at all.
     */
    def bfs(p: T => Boolean = always): Iterable[T] = bfsQueue(p, node)

    def doMap[U](f: T => U): Tree[U] = {
      def inner( tn: Node[T]): Tree[U] = Tree(f(tn.key), tn.children.map(inner))

      inner(node)
    }

    type Item = Either[Node[T], T]

    private def inOrderFunc[V](ws: Seq[Item], tn: Node[T]) = tn.children match {
      case Nil =>
        Right(tn.key) +: ws
      case first :: rest =>
        Left(first) +: Right(tn.key) +: (rest.map(Left(_)) ++ ws)
    }

    private def postOrderFunc[V](ws: Seq[Item], tn: Node[T]) = tn.children.map(Left(_)) ++ (Right(tn.key) +: ws)

    @tailrec
    private final def traverse[V](visitor: V, p: T => Boolean, f: (Seq[Item], Node[T]) => Seq[Item])(work: Seq[Item])(implicit tVv: Visitor[T, V]): V =
      work match {
        case Nil => visitor
        case w :: ws => w match {
          case Left(tn) =>
            if (tn.filter(p))
              traverse(visitor, p, f)(f(ws, tn))
            else
              traverse(visitor, p, f)(ws)
          case Right(t) =>
            traverse(tVv.visit(visitor, t), p, f)(ws)
        }
      }
  }

  @tailrec
  private final def traversePre[T, V](visitor: V, p: T => Boolean, nodes: Seq[Node[T]])(implicit tVv: Visitor[T, V]): V = nodes match {
    case Nil => visitor
    case tn :: tns if tn.filter(p) => traversePre(tVv.visit(visitor, tn.key), p, tn.children ++ tns)
    case _ :: tns => traversePre(visitor, p, tns)
  }

  private def bfsQueue[T](p: T => Boolean, n: Node[T]): Queue[T] = bfs(Queue.empty[T], p)(Queue(n))(new QueueVisitor[T] {})

  @tailrec
  private final def bfs[T, V](visitor: V, p: T => Boolean)(queue2: Queue[Node[T]])(implicit tVv: Visitor[T, V]): V =
    queue2.dequeueOption match {
      case None => visitor
      case Some((tn, q)) =>
        if (tn.filter(p))
          bfs(tVv.visit(visitor, tn.key), p)(tn.children.foldLeft(q)(_.enqueue(_)))
        else bfs(visitor, p)(q)
    }

  private val always: Any => Boolean = _ => true
}
