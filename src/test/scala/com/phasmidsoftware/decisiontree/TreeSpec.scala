package com.phasmidsoftware.decisiontree

import com.phasmidsoftware.util.{Loggable, Show}
import org.scalatest.matchers.should
import org.scalatest.{BeforeAndAfterEach, flatspec}

case class State(x: Int, y: Int)

object State {

  implicit object OrderingState extends Ordering[State] {
    def compare(x: State, y: State): Int = {
      val z = implicitly[Ordering[Int]]
      val cf = z.compare(x.x, y.x)
      if (cf == 0) z.compare(x.y, y.y)
      else cf
    }
  }

  implicit object LoggableState extends Loggable[State] {
    def toLog(t: State): String = t.toString
  }

  implicit object ShowState extends Show[State] {
    def show(t: State): String = t.toString
  }

  implicit object GoalDrivenState extends GoalDriven[State] {
    def goalAchieved(t: State): Boolean = t.x > 3

    def goalImpossible(t: State, moves: Int): Boolean = t.y > 4
  }

  implicit object ExpandableState extends Expandable[State] {
    def successors(t: State): List[State] = List(State(t.x + 1, t.x + 2))
  }

}

class TreeSpec extends flatspec.AnyFlatSpec with BeforeAndAfterEach with should.Matchers {

  override def beforeEach(): Unit = {
  }

  override def afterEach(): Unit =  {

  }

  behavior of "TreeSpec"

  it should "expand" in {
    val state = State(0, 0)
    val target = Tree[State](state)
    val z: StateNode[State] = target.expand(100)
    z.depthFirstTraverse shouldBe List(State(4, 5), State(3, 4), State(2, 3), State(1, 2), State(0, 0))
  }

  it should "apply" in {
    val state = State(5, 8)
    val stateNode: StateNode[State] = StateNode[State](state, None, Nil)
    val target: Tree[State] = new Tree[State](stateNode)
    target.root.state shouldBe state
  }
}
