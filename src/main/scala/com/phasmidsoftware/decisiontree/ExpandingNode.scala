/*
 * Copyright (c) 2019. Phasmid Software
 */

package com.phasmidsoftware.decisiontree

import com.phasmidsoftware.decisiontree.tree.Goal
import com.phasmidsoftware.util.{Loggable, Loggables, Show}
import org.slf4j.{Logger, LoggerFactory}

/**
 * This case class is used for the result of expand method of ExpandingNode.
 *
 * @param evaluated either Some(expanding node) or: None
 * @param unevaluated a sequence of unevaluated states.
 * @tparam T the underlying type of the expansion.
 */
case class Expansion[T](evaluated: Option[ExpandingNode[T]], unevaluated: Seq[T])

/**
 *
 * @param t        the value of this Node.
 * @param so       an optional indication of the solution represented by this sub-tree.
 * @param children the children nodes of this Node.
 * @tparam T the underlying type of the nodes, for which there must be evidence of:
 *           Expandable;
 *           GoalDriven;
 *           Ordering (order is based on the number of "moves" required to reach a given t);
 *           Loggable.
 */
abstract class ExpandingNode[T: Expandable : GoalDriven : Ordering : Loggable : Show]
(val t: T, val so: Option[T], val children: List[ExpandingNode[T]]) extends Node[T] {

  /**
   * Method to add the given node to the children of this Node.
   *
   * @param node the node to add as a child.
   * @return a copy of this Node but with node as an additional child, and the so value corresponding to this.
   */
  override def :+(node: Node[T]): ExpandingNode[T] = unit(t, children :+ node)

  /**
   * Method to add the given tns to the children of this Node.
   *
   * @param tns the tns to add as additional children.
   * @return a copy of this Node but with tns as additional children, and the so value corresponding to this.
   */
  override def ++(tns: Seq[Node[T]]): Node[T] = unit(t, children ++ tns)

  /**
   * Method to add the given x-value to the children of this Node.
   *
   * @param x the x value to be turned into a Node which is then :+'d to this Node.
   * @return a copy of this Node but with x as an additional child value, and the so value corresponding to this.
   */
  override def :+(x: T): ExpandingNode[T] = this :+ unit(x)

  /**
   * Method to form a Node from a T with the given children and with so based on the value in this.
   *
   * @param _t  the given value of T.
   * @param tns the nodes which will be the children of the result.
   * @return a new Node based on t and tns, and the so value corresponding to this.
   */
  def unit(_t: T, tns: List[Node[T]]): ExpandingNode[T] = unit(_t, so, tns)

  /**
   * Method to form a Node from a T.
   *
   * @param _t  the given value of T.
   * @param _so an optional T representing a solution.
   * @param tns the nodes which will be the children of the result.
   * @return a new Node based on t and tns.
   */
  def unit(_t: T, _so: Option[T], tns: Seq[Node[T]]): ExpandingNode[T]

  /**
   * Method to expand a branch of a tree, by taking this ExpandingNode and (potentially) adding child nodes which are themselves recursively expanded.
   * The algorithm operates in a depth-first-search manner.
   *
   * CONSIDER make this tail-recursive
   *
   * @param _so   the (optional) currently satisfied goal.
   * @param moves the number of possible moves remaining.
   * @return an Option of ExpandingNode[T]:
   *         None => we have run out of moves
   *         Some(n) => n is either this but marked as solved; or this with expanded children added.
   */
  def expand(_so: Option[T], moves: Int): Option[ExpandingNode[T]] = {
    val te = implicitly[Expandable[T]]
    if (moves < 0)
      None
    else if (te.runaway(t)) {
      Console.println(s"expand: runaway condition detected for $t")
      None
    }
    else te.result(t, _so, moves) match {
        case Left(b) =>
            Some(solve(b)) // XXX terminating condition found--mark and return this.
        case Right(Nil) =>
            None // XXX situation with no descendants: return None.
        case Right(ts) =>
            // XXX normal situation with (possibly empty) descendants?
            // XXX Recursively expand them, ensuring that the elements are unique.
            import com.phasmidsoftware.util.SmartValueOps._
            val verifiedStates = ts.invariant(z => z.distinct.size == z.size)
            Some(expandSuccessors(verifiedStates, moves - 1, _so))
    }
  }

    /**
     * Method to expand a branch of a tree, by taking this ExpandingNode and (potentially) adding child nodes which are themselves recursively expanded.
     * The algorithm operates in a depth-first-search manner.
     *
     * @param _so   the (optional) currently satisfied goal.
     * @param moves the number of possible moves remaining.
     * @return an Option of ExpandingNode[T]:
     *         None => we have run out of moves
     *         Some(n) => n is either this but marked as solved; or this with expanded children added.
     */
    def bfsWithExpand(_so: Option[T], moves: Int): Option[ExpandingNode[T]] = {
        val te = implicitly[Expandable[T]]
        if (moves < 0)
            None
        else if (te.runaway(t)) {
            Console.println(s"expand: runaway condition detected for $t")
            None
        }
        else te.result(t, _so, moves) match {
            case Left(b) =>
                Some(solve(b)) // XXX terminating condition found--mark and return this.
            case Right(Nil) =>
                None // XXX situation with no descendants: return None.
            case Right(ts) =>
                // XXX normal situation with (possibly empty) descendants?
                // XXX expand them, ensuring that the elements are unique.
                import com.phasmidsoftware.util.SmartValueOps._
                val verifiedStates: List[T] = ts.invariant(z => z.distinct.size == z.size)
                Some(expandSuccessors(verifiedStates, moves - 1, _so))
        }
    }

    /**
     * Method to replace node x with node y in this sub-tree.
     * Additionally, if y is decided, then we mark the result as decided.
     *
     * @param x the node to be replaced.
     * @param y the node with which to replace the given node.
   * @return a copy of this Node, but with x replaced by y.
   */
  override def replace(x: Node[T], y: Node[T]): ExpandingNode[T] = y match {
      // TODO use corresponding code from Node.scala
    case yE: ExpandingNode[T] =>
      val result = if (x == yE) this
      else if (children contains x) unit(t, children.map(n => if (n eq x) yE else n)) // NOTE: this picks up the value of so from this
      else unit(t, children map (n => n.replace(x, yE))) // NOTE: this picks up the value of so from this
      yE.so match {
        case Some(b) => result.solve(b)
        case None => result
      }
    case _ => throw NodeException(s"replace cannot operate unless y is an ExpandingNode")
  }

  /**
   * Construct a new ExpandingNode based on the given value of t and the value of so from this.
   *
   * @param t the given value of T.
   * @return a new non-decided Node based on t, but with no children.
   */
  override def unit(t: T): ExpandingNode[T] = unit(t, so, Nil)

  /**
   * Method to add the given values to the children of this Node.
   *
   * @param ts the values to add as additional child values.
   * @return a copy of this Node but with ts as additional child values.
   */
  override def :+(ts: Seq[T]): ExpandingNode[T] = (this ++ (ts map unit)).asInstanceOf[ExpandingNode[T]]

  /**
   * Private Methods...
   */

  /**
   * Expand the successors in the list ts to form a new node.
   *
   * @param ts    the list of states.
   * @param moves the remaining number of moves.
   * @param _so   any satisfied state.
   * @return a new ExpandingNode[T].
   */
  private def expandSuccessors(ts: List[T], moves: Int, _so: Option[T]) = {

    def getBestSolution(_sor: Option[T]) = _sor match {
      case Some(sr) => Some(_so match {
        case Some(ss) => if (implicitly[Ordering[T]].compare(sr, ss) < 0) sr else ss
        case None => sr
      })
      case None => _so
    }

    def doExpansion(r: ExpandingNode[T], t: T): ExpandingNode[T] = unit(t, None, Nil).expand(getBestSolution(r.so), moves) match {
      case None => r // expansion came up empty // CONSIDER should we return node based on t here?
      case Some(n) => n.so match {
        case Some(g) => r.solve(g) :+ n // goal achieved: add it as a child and mark result as goal achieved
        case None => r
      }
    }

    ts.foldLeft[ExpandingNode[T]](this)(doExpansion)
  }

  /**
   * Method to mark this Node with an optional T (i.e. representing a goal that has been reached).
   *
   * @param success the T value corresponding to the goal reached.
   * @return a new copy of this Node or the same but with so set to Some(success).
   */
  private def solve(success: T) = so match {
    case Some(x) =>
      if (x == success) this
      else if (implicitly[Ordering[T]].compare(x, success) < 0) unit(t, Some(success), children) // TODO check
      else this
    case _ => unit(t, Some(success), children)
  }
}

object ExpandingNode extends Loggables {

  /**
   * TEST this doesn't seem to be used anywhere.
   *
   * @tparam T the underlying type.
   * @return a Loggable of ExpandingNode[T].
   */
  def expandingNodeLogger[T: Loggable]: Loggable[ExpandingNode[T]] = (t: ExpandingNode[T]) => {

    val wT = implicitly[Loggable[T]].toLog(t.t)
    val wDecided = t.so match {
      case Some(b) => s" (decided=$b)"
      case None => ""
    }
    val wFollowers = s" with ${t.children.size}"
    wT + wDecided + wFollowers
  }
}

/**
 * This is the trait (actually, it's the base of a type-class) which allows
 * the application to program the manner of expanding a tree.
 *
 * @tparam T the underlying type (matches the T of a Node).
 */
trait Expandable[T] {
  /**
   * Method to yield the successors (i.e. children) of the underlying type T for purposes of node expansion.
   *
   * @param t the value of T.
   * @return a List[T] containing the successors (children) of T.
   */
  def successors(t: T): List[T]

  import com.phasmidsoftware.util.SmartValueOps._

  /**
   * Method to yield the result (i.e. children) of the underlying type T.
   *
   * Either a list of new child nodes is returned or we return a Boolean to signify the reaching of a goal.
   * The value of the Boolean signifies whether it was a positive goal or a negative goal.
   * For example, when the T values represent states of play in Bridge,
   * a positive goal is that the protagonist (declaring side) wins a specified number of tricks, say nine.
   * A negative goal would be when the antagonist (defending side) wins sufficient tricks to make the protagonist's goal
   * impossible, i.e. when they win five tricks.
   *
   * @param t     the value of T.
   * @param to    an optional T which represents an achieved goal state.
   * @param moves the number of possible moves remaining in which to achieve the goal.
   * @return an Either of T or List[T].
   *         If the return is Right(List(...)) then the content of the option is the list of (new) children.
   *         If the result is Right(Nil), it signifies that the given value of t holds no promise and therefore should not be further expanded.
   *         If the return is Left(T), it signifies that we have reached a solution (goal) represented by the value of T.
   */
  def result(t: T, to: Option[T], moves: Int)(implicit ev1: GoalDriven[T], ev2: Ordering[T], ev3: Show[T]): Either[T, List[T]] = {
    val sT = ev3.show(t)
    count += 1
    if (count % 100000 == 0) logger.debug(s"Testing ${count}th state: $sT")
    if (ev1.goalAchieved(t)) {
      logger.debug(s"Goal achieved for state: $sT")
      Left(t)
    }
    else if (ev1.goalOutOfReach(t, to, moves)) {
      logger.trace(s"Goal impossible for state: $sT")
      Right(Nil)
    }
    else Right(successors(t))
  }

  /**
   * Method to check whether the value of T indicates a runaway condition.
   *
   * @param t the value of T.
   * @return true if running away, else false.
   */
  def runaway(t: T): Boolean = false // NOTE: if your application takes a very long time expanding, you might want to set this to true on some condition

  var count = 0

  /**
   * Method to initialize this Expandable.
   */
  def init(): Unit = {
    count = 0
  }
}

object Expandable {
  val logger: Logger = LoggerFactory.getLogger(getClass)

//  def cache[T]: mutable.HashMap[(T, Option[T], Int), Either[T, List[T]]] = mutable.HashMap[(T, Option[T], Int), Either[T, List[T]]]()
}

case class ExpandingNodeException(str: String) extends Exception(str)

trait GoalDriven[T] {
  /**
   * Method to test whether a T value satisfies the goal.
   *
   * @param t the T value.
   * @return true if the goal is achieved by t
   */
  def goalAchieved(t: T): Boolean

  /**
   * Method to test whether a T value can never satisfy the goal.
   *
   * @param t     the T value.
   * @param moves the number of remaining moves in which the goal might be reached.
   * @return true if the goal can never be achieved by t or its descendants (expansions).
   */
  def goalImpossible(t: T, moves: Int): Boolean

  /**
   * Method to determine if it's possible for a T value to reach a "better" goal than the value, if any, of so.
   *
   * @param t the T value.
   * @param s another T value which satisfies the goal (and has already been achieved by another branch of the tree).
   * @return if t can reach a better goal than s. Typically, this will be based on the Ordering of s and t.
   */
  def goalConditional(t: T, s: T)(implicit ev: Ordering[T]): Boolean = ev.compare(s, t) > 0

  /**
   * Determine if it is impossible to reach the goal from t when an optional alternative goal (so) has already been reached.
   *
   * @param t     the value of T to test.
   * @param so    an optional alternative goal which has already been reached by another branch.
   * @param moves the number of remaining moves in which the goal might be reached.
   * @return goalImpossible(t, moves) or, if so is Some(s) then !goalConditional(t, s).
   */
  def goalOutOfReach(t: T, so: Option[T], moves: Int)(implicit ev: Ordering[T]): Boolean = goalImpossible(t, moves) || (so match {
    case Some(s) => !goalConditional(t, s)
    case None => false
  })

  /**
   * Interpret this GoalDriven object as a Goal.
   * NOTE: we ignore the impossible conditions for now.
   *
   * @return a Goal[T]
   */
  def goal: Goal[T] = Goal.goal(t => goalAchieved(t))

}
