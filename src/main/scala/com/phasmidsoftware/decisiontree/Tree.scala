/*
 * Copyright (c) 2019. Phasmid Software
 */

package com.phasmidsoftware.decisiontree

import com.phasmidsoftware.util.{Loggable, Output, Outputable, Show}

import scala.language.postfixOps

/**
 * TODO we can generalize this class and move it into the tree package.
 *
 * This class represents a tree of State nodes.
 * Each State represents a sequence of card plays and current score of NS vs EW tricks.
 *
 * @param root the root node of the tree.
 */
class Tree[State](val root: StateNode[State]) extends Outputable[Unit] {

  /**
   * Expand the states of this Tree.
   *
   * @param levels the number of levels to enumerate.
   * @return a StateNode.
   */
  def expand(levels: Int): StateNode[State] =
    root.expand(None, levels) match {
      case Some(n) => n.asInstanceOf[StateNode[State]]
      case None => throw NodeException(s"unable to enumerate $levels plays for tree headed by ${root.state}")
    }

  /**
   * Output this Tree to the given Output.
   *
   * @param output the output to append to.
   * @param xo     an optional value of X, defaulting to None.
   * @return a new instance of Output.
   */
  def output(output: Output, xo: Option[Unit] = None): Output = root.output(output)
}

object Tree {
  /**
   * Method to create a Tree from a State.
   *
   * @param state the given State.
   * @return a new Tree based on the state as its root.
   */
  def apply[State: Ordering : Loggable](state: State)(implicit ev1: Expandable[State], ev2: GoalDriven[State], ev3: Show[State]): Tree[State] =
    new Tree[State](StateNode[State](state, None, Nil))
}

/**
 * This represents a node in the deal analysis tree.
 *
 * @param state     a Trick/Deal combination: the trick is in general incomplete: each node represents a different play.
 * @param so        an optional indication of the solution represented by this sub-tree.
 * @param followers the children of this node, i.e. the nodes which will follow.
 */
case class StateNode[State: Ordering : Loggable](state: State, override val so: Option[State], followers: List[StateNode[State]])(implicit ev1: Expandable[State], ev2: GoalDriven[State], ev3: Show[State])
  extends ExpandingNode[State](state, so, followers) {

  /**
   *
   * Method to form a Node from a T.
   *
   * CONSIDER rename decided
   *
   * @param t   the given value of T.
   * @param so  an optional indication of the solution represented by this sub-tree.
   * @param tns the nodes which will be the children of the result.
   * @return a new Node based on t and tns.
   */
  def unit(t: State, so: Option[State], tns: Seq[Node[State]]): StateNode[State] = StateNode(t, so, tns.asInstanceOf[List[StateNode[State]]])

  /**
   * Method to form a Node from a State.
   *
   * @param t the given value of State.
   * @return a new Node based on t, but with no children.
   */
  override def unit(t: State): StateNode[State] = super.unit(t).asInstanceOf[StateNode[State]]

  /**
   * NOTE: not sure why we need this now but didn't before.
   *
   * @param t   the given value of T.
   * @param tns the nodes which will be the children of the result.
   * @return a new Node based on t and tns.
   */
  def unit(t: State, tns: Seq[Node[State]]): StateNode[State] = unit(t, None, tns)

  /**
   * Method to add the given tree nodes to the children of this Node.
   *
   * @param tns the trick nodes to add as additional children.
   * @return a copy of this Node but with tns as additional children.
   */
  override def ++(tns: Seq[Node[State]]): StateNode[State] = super.++(tns).asInstanceOf[StateNode[State]]

  /**
   * Method to add the given node to the children of this Node.
   *
   * @param node the node to add as a child.
   * @return a copy of this Node but with node as an additional child.
   */
  override def :+(node: Node[State]): StateNode[State] = super.:+(node).asInstanceOf[StateNode[State]]

  /**
   * Method to add the given x-value to the children of this Node.
   *
   * @param x the x value to be turned into a Node which is then :+'d to this Node.
   * @return a copy of this Node but with x as an additional child value.
   */
  override def :+(x: State): StateNode[State] = super.:+(x).asInstanceOf[StateNode[State]]

  /**
   * Method to replace a node of this tree with the given node and to return the resulting tree.
   *
   * @param x the node to be replace.
   * @param y the node with which to replace the given node.
   * @return a copy of this StateNode, but with node replaced by replacement.
   */
  override def replace(x: Node[State], y: Node[State]): StateNode[State] = super.replace(x, y).asInstanceOf[StateNode[State]]

  /**
   * Method to append a node to the given node of this tree and to return the resulting tree.
   *
   * @param node     the node to which we append the appendee.
   * @param appendee the node to be appended to the given node.
   * @return a copy of this StateNode, but with appendee appended to node
   */
  override def append(node: Node[State], appendee: Node[State]): StateNode[State] = super.append(node, appendee).asInstanceOf[StateNode[State]]

  /**
   * Method to append a state to the given node of this tree and to return the resulting tree.
   *
   * @param node  the node to which we append the appendee.
   * @param state the state to be appended to the given node.
   * @return a copy of this StateNode, but with appendee appended to node
   */
  override def append(node: Node[State], state: State): StateNode[State] = super.append(node, state).asInstanceOf[StateNode[State]]

}



