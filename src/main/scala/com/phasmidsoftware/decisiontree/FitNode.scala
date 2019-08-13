/*
 * Copyright (c) 2019. Phasmid Software
 */

package com.phasmidsoftware.decisiontree

/**
 * Type class to model the behavior of something with fitness.
 *
 * @tparam X the underlying type.
 */
trait Fitness[X] {

  /**
   * Method to evaluate the fitness of an X.
   *
   * @param x the value whose fitness is to be evaluated.
   * @return the fitness of x as a Double.
   */
  def fitness(x: X): Double

}

/**
 * Abstract class representing a Node with a fitness.
 *
 * @param t        the value of this Node.
 * @param decided  indicator of the node having terminated the expansion of the tree.
 * @param children the children nodes of this Node.
 * @tparam X the underlying type of the nodes, for which there must be evidence of Fitness.
 */
abstract class FitNode[X: Fitness](val t: X, val decided: Option[Boolean], val children: List[Node[X]]) extends Node[X] with Ordered[FitNode[X]] {

  /**
   * Compare this node with that node as far as fitness is concerned.
   *
   * @param that another FitNode.
   * @return 0, 1 or 2 as appropriate according to order of this fitness versus that fitness.
   */
  def compare(that: FitNode[X]): Int = {
    val xf = implicitly[Fitness[X]]
    implicitly[Ordering[Double]].compare(xf.fitness(t), xf.fitness(that.t))
  }
}

case class FitNodeException(str: String) extends Exception(str)
