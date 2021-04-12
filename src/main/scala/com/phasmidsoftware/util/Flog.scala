/*
 * Copyright (c) 2019. Phasmid Software
 */

package com.phasmidsoftware.util

import org.slf4j.LoggerFactory

import scala.reflect.ClassTag

/**
 * Simple functional logging utility.
 *
 * Here are the steps you need to follow to enable logging:
 *
 * <ol>
 * <li>In your code, somewhere in scope (using "implicits" scope rules), import Flog._</li>
 * <li>Create a String which will form the log message, follow it with "!!" and follow that with the expression you want to log.</li>
 * <li>Most of the time, this is all you need to do.</li>
 * <li>If you wish to override the logging function, then declare something like the following:
 * <code>implicit def logFunc(w: String): LogFunction = LogFunction(p-r-i-n-t-l-n)</code> (take out the dashes, obviously).
 * In this case, you will need to explicitly construct an instance of Flogger from your message string, such as:
 * <code>Flogger(getString)(logFunc)</code>
 * Follow this with !! and the expression, as usual.
 * </li>
 * <li>If you wish to override the default LogFunction, for example to log according to a particular class,
 * then you can also set it (it is declared as a var):
 * <code>Flog.loggingFunction = Flog.getLogger[FlogSpec]</code>
 * </li>
 * </ol>
 * <p/>
 * <p>
 * There are several ways to turn logging off (temporarily or permanently) once you've added the log expressions:
 * <dl>
 * <dt>(1A)</dt> <dd>replace the !! method with the |! method for each expression you wish to silence;</dd>
 * <dt>(1B)</dt> <dd>define an implicit LogFunction which does nothing (but this will involve explicitly constructing a Flogger, as described above);</dd>
 * <dt>(2)</dt> <dd>set Flog.enabled = false in your code (silences all logging everywhere);</dd>
 * <dt>(3)</dt> <dd>remove the !! expressions.</dd>
 */
object Flog {

  /**
   * Implicit class to implement functional logging.
   *
   * If you are using the default logging function (Flog.loggingFunction), then you can instantiate and utilize
   * a new instance of Flogger simply by applying the "!!" operator to a String.
   * However, if you are using a locally defined value of the logging function, you will have to instantiate a Flogger
   * explicitly (see FlogSpec for examples).
   *
   * @param message the message itself which will be evaluated only if enabled is actually turned on.
   * @param logFunc the logging function to be used for this log message (defaults to Flog.loggingFunction).
   */
  implicit class Flogger(message: => String)(implicit logFunc: LogFunction = Flog.loggingFunction) {
    /**
     * Method to generate a log entry.
     * Logging is performed as a side effect.
     * Rendering of the x value is via the toLog method of the implicit Loggable[X].
     *
     * @param x the value to be logged.
     * @tparam X the type of x, which must provide evidence of being Loggable.
     * @return the value of x.
     */
    def !![X: Loggable](x: => X): X = Flog.logLoggable(logFunc, message)(x)

    /**
     * Method to generate a log entry.
     * Logging is performed as a side effect.
     * Rendering of the x value is via the toLog method of the implicit Loggable[X].
     *
     * CONSIDER do we need this? We have this covered in Loggables, surely?
     *
     * @param xs the value to be logged.
     * @tparam X the type of x, which must provide evidence of being Loggable.
     * @return the value of x.
     */
    def !|[X: Loggable](xs: => Iterable[X]): Iterable[X] = {
      // CONSIDER using Loggables....
      implicit object loggableIterableX extends Loggable.LoggableIterable[X]
      Flog.logLoggable(logFunc, message)(xs)
      xs
    }

    /**
     * Method to generate a log entry.
     * Logging is performed as a side effect.
     * Rendering of the x value is via the toString method.
     *
     * @param x the value to be logged.
     * @tparam X the type of x.
     * @return the value of x.
     */
    def !|[X](x: => X): X = Flog.logX(logFunc, message)(x)

    /**
     * Method to simply return the value of x without any logging.
     *
     * @param x the value.
     * @tparam X the type of x.
     * @return the value of x.
     */
    def |![X](x: => X): X = x
  }

  /**
   * The master switch.
   * Logging only occurs if this variable is true.
   */
  var enabled = true

  /**
   * The default logging function which logs to the debug method of the logger for the Flogger class.
   */
  implicit var loggingFunction: LogFunction = getLogger[Flogger]

  def getLogger[T: ClassTag]: LogFunction = LogFunction(LoggerFactory.getLogger(implicitly[ClassTag[T]].runtimeClass).debug)

  /**
   * Method to generate a log message based on x, pass it to the logFunc, and return the x value.
   * The value of x will be rendered as a String but invoking toLog on the implicit value of Loggable[X].
   *
   * @param logFunc the logging function.
   * @param prefix  the message prefix.
   * @param x       the value to be logged and returned.
   * @tparam X the underlying type of x, which is required to provide evidence of Loggable[X].
   * @return the value of x.
   */
  def logLoggable[X: Loggable](logFunc: LogFunction, prefix: => String)(x: => X): X = {
    lazy val xx: X = x
    if (enabled) logFunc(s"log: $prefix: ${implicitly[Loggable[X]].toLog(xx)}")
    xx
  }

  /**
   * Method to generate a log message, pass it to the logFunc, and return the x value.
   * The difference between this method and the logLoggable method is that the value of x will be rendered as a String,
   * simply by invoking toString.
   *
   * @param logFunc the logging function.
   * @param prefix  the message prefix.
   * @param x       the value to be logged and returned.
   * @tparam X the underlying type of x.
   * @return the value of x.
   */
  def logX[X](logFunc: LogFunction, prefix: => String)(x: => X): X = {
    lazy val xx: X = x
    if (enabled) logFunc(s"log: $prefix: $xx")
    xx
  }
}

case class LogFunction(f: String => Any) {
  def apply(w: String): Unit = f(w)
}