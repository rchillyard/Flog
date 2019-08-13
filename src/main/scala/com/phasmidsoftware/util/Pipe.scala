/*
 * Copyright (c) 2019. Phasmid Software
 */

package com.phasmidsoftware.util

import org.slf4j.Logger

import scala.util.control.NonFatal

/**
 * Case class to define an identity flow of X => X, which can be sampled or "tapped" by a side-effect defined by (X => Unit) function "tee",
 * according to the value of the <code>predicate<code>.
 * The purpose of this case class is to facilitate side-effects which do not interrupt the structure of a functional expression.
 * In the default situation where logExceptions is true, Pipe guarantees that non-fatal exceptions in tee or predicate will not interrupt the flow--
 * instead, such exceptions will be logged.
 *
 * @param predicate     a filter function (X=>Boolean) which causes tee to be invoked only when the X input value satisfies the predicate (defaults to always true).
 * @param tee           the sampling function (X=>Unit) which operates as a side-effect (defaults to a no-op), and which is called only if the predicate evaluates to true.
 * @param logExceptions if set to true (the default), the Pipe will throw an exception only if it's fatal--otherwise, it will log it.
 *                      if set to false, any thrown exception will be wrapped in a new PipeException which is thrown.
 * @param logger        (implicit) the logger which should be used if necessary.
 * @tparam X the input and output type of the apply function of the pipe.
 */
case class Pipe[X](predicate: X => Boolean = Pipe.always, tee: X => Unit = Pipe.noop _, logExceptions: Boolean = true)(implicit logger: Logger) extends (X => X) {
  self =>

  /**
   * The apply function of this Pipe.
   *
   * @param x the given value of x.
   * @return the same value of x but after invoking the side-effect defined by tee.
   * @throws PipeException if predicate(x) or tee(x) throws a non-fatal exception, AND if logExceptions = false.
   */
  override def apply(x: X): X = {
    lazy val exceptionMessage = "Pipe.apply(): an exception was thrown in either predicate or tee"
    try if (predicate(x))
      tee(x)
    catch {
      case NonFatal(e) =>
        if (logExceptions) logger.warn(exceptionMessage, e)
        else throw PipeException(exceptionMessage, e)
    }
    x
  }

  /**
   * Method to yield a new Pipe with the predicate inverted.
   *
   * @return a new Pipe[X] which will operate on all X values which fail predicate.
   */
  def not: Pipe[X] = new Pipe[X](self.predicate.andThen(b => !b), tee, logExceptions)

  /**
   * Method to yield a new Pipe where the predicate is conjoined with another predicate f.
   *
   * @param f a filter function (X=>Boolean) which causes tee to be invoked only when the X input value satisfies both predicate and f.
   * @return a new Pipe[X] which will operate on all X values which satisfy predicate and f.
   */
  def and(f: X => Boolean): Pipe[X] = new Pipe[X](Pipe.and(self.predicate, f), tee, logExceptions)

  /**
   * Method to yield a new Pipe where the predicate is disjoined with another predicate f.
   *
   * @param f a filter function (X=>Boolean) which causes tee to be invoked only when the X input value satisfies either predicate or f.
   * @return a new Pipe[X] which will operate on all X values which satisfy predicate or f.
   */
  def or(f: X => Boolean): Pipe[X] = new Pipe[X](Pipe.or(self.predicate, f), tee, logExceptions)
}

object Pipe {
  private def and[X](predicate: X => Boolean, f: X => Boolean): X => Boolean = x => predicate(x) && f(x)

  private def or[X](predicate: X => Boolean, f: X => Boolean): X => Boolean = x => predicate(x) || f(x)

  private def always[X]: X => Boolean = _ => true

  private def noop[X](x: X): Unit = ()
}

case class PipeException(msg: String, e: Throwable = null) extends Exception(s"Pipe exception: $msg", e)