/*
 * Copyright (c) 2019. Phasmid Software
 */

package com.phasmidsoftware.flog

import org.slf4j.{Logger, LoggerFactory}

import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

/**
 * Simple functional logging class.
 *
 * Here are the steps you need to follow to enable logging:
 *
 * <ol>
 * <li>Instantiate an new instance of Flog, specifying the parameter value described below--or
 * simply invoke the apply method: <code>Flog()</code>.
 * <dl>
 * <dt>loggingFunction</dt><dd>by default, this will be a function which uses the debug method of a logger based on this class;</dd>
 * <dt>errorFunction</dt><dd>by default, this will be a function which uses the warn method of a logger based on this class;</dd>
 * </dl>
 * Assuming that you named the variable "flog", then "import flog._"</li>
 * <li>Create a String which will form the log message, follow it with "!!" and follow that with the expression you want to log.
 * The construct in this form (including the message and "!!") will yield the value of the expression.</li>
 * <li>Most of the time, this is all you need to do (see README.md for much more information on this).</li>
 * </ol>
 * <p>
 * There are several ways to turn logging off (temporarily or permanently) once you've added the log expressions:
 * <dl>
 * <dt>(1A)</dt> <dd>replace the !! method with the |! method for each expression you wish to silence;</dd>
 * <dt>(2)</dt> <dd>use a disabled Flog with flog.disabled (silences all flogging everywhere);</dd>
 * <dt>(3)</dt> <dd>remove the !! expressions.</dd>
 * </dl>
 * </p>
 * 
 * @param loggingFunction the LogFunction which is to be used by this Flog.
 */
case class Flog(loggingFunction: LogFunction, errorFunction: LogFunction) {

  import Flog._

  /**
   * Implicit class to implement functional logging.
   *
   * If you are using the default logging function (Flog.loggingFunction), then you can instantiate and utilize
   * a new instance of Flogger simply by applying the "!!" operator to a String.
   *
   * @param message the message itself which will be evaluated only if enabled is actually turned on.
   */
  implicit class Flogger(message: => String) extends Loggables {
    /**
     * Method to generate a log entry for a (Loggable) value of X.
     * Logging is performed as a side effect.
     * Rendering of the x value is via the toLog method of the implicit Loggable[X].
     *
     * @param x the value to be logged.
     * @tparam X the type of x, which must provide implicit evidence of being Loggable.
     * @return the value of x.
     */
    def !![X: Loggable](x: => X): X = logLoggable(message)(x)

    /**
     * Method to generate a log entry for a Map[K, V].
     * Logging is performed as a side effect.
     *
     * @param kVm the Map to be logged.
     * @tparam K the type of the map keys, which must provide implicit evidence of being Loggable.
     * @tparam V the type of the map values, which must provide implicit evidence of being Loggable.
     * @return the value of x.
     */
    def !![K: Loggable, V: Loggable](kVm: => Map[K, V]): Map[K, V] = {
      implicit val g: Loggable[(K, V)] = kVLoggable
      tee[Map[K, V]](y => logLoggable(message)(toLog(y)))(kVm)
    }

    /**
     * Method to generate a log entry for an Iterable of a (Loggable) X.
     * Logging is performed as a side effect.
     * Rendering of the x value is via the toLog method of the implicit Loggable[X].
     *
     * @param xs the Iterable value to be logged.
     * @tparam X the underlying type of x, which must provide implicit evidence of being Loggable.
     * @return the value of x.
     */
    def !![X: Loggable](xs: => Iterable[X]): Iterable[X] = tee[Iterable[X]](y => logLoggable(message)(toLog(y)))(xs)

    /**
     * Method to generate a log entry for an Option of a (Loggable) X.
     * Logging is performed as a side effect.
     * Rendering of the x value is via the toLog method of the implicit Loggable[X].
     *
     * @param xo the optional value to be logged.
     * @tparam X the underlying type of x, which must provide implicit evidence of being Loggable.
     * @return the value of x.
     */
    def !![X: Loggable](xo: => Option[X]): Option[X] = tee[Option[X]](y => logLoggable(message)(toLog(y)))(xo)

    /**
     * Method to generate a log entry for a type which is not itself Loggable.
     * Logging is performed as a side effect.
     * Rendering of the x value is via the toString method.
     *
     * @param x the value to be logged.
     * @tparam X the type of x.
     * @return the value of x.
     */
    def !|[X](x: => X): X = logX(message)(x)

    /**
     * Method to simply return the value of x without any logging.
     *
     * @param x the value.
     * @tparam X the type of x.
     * @return the value of x.
     */
    def |![X](x: => X): X = x

    /**
     * Method to log the value xy (a Try[X]) but which logs any failures using the errorFunction rather than
     * the loggerFunction.
     * NOTE that the returned value, if xy is a Failure, is not exactly the same as xy.
     *
     * @param xy an instance of Try[X].
     * @tparam X the underlying type of xy.
     * @return if xy is successful, then xy, otherwise if Failure(e) then Failure(LoggedException(e)).
     */
    def !!![X: Loggable](xy: Try[X]): Try[X] = xy.transform(Success(_), e => {
      errorFunction f s"$message $e"
      Failure(LoggedException(e))
    })

    private def toLog[X: Loggable](y: Option[X]): String = optionLoggable[String].toLog(y map implicitly[Loggable[X]].toLog)

    private def toLog[X: Loggable](y: Iterable[X]): String = iterableLoggable[String]().toLog(y map implicitly[Loggable[X]].toLog)

    private def toLog[K, V](y: Map[K, V])(implicit kVl: Loggable[(K, V)]): String = iterableLoggable[String]("{}").toLog(y map kVl.toLog)
  }

  /**
   * Use this method to create a new Flog based on the given logging function.
   *
   * @param logFunc an instance of LogFunction.
   * @return a new instance of Flog.
   */
  def withLogFunction(logFunc: LogFunction): Flog = Flog(logFunc, errorFunction)

  /**
   * Use this method to create a new Flog with logging disabled.
   *
   * @return a new instance of Flog.
   */
  def disabled: Flog = withLogFunction(loggingFunction.disable)

  /**
   * Method to generate a log message based on x, pass it to the logFunc, and return the x value.
   * The value of x will be rendered as a String but invoking toLog on the implicit value of Loggable[X].
   *
   * @param prefix the message prefix.
   * @param x      the value to be logged and returned.
   * @tparam X the underlying type of x, which is required to provide evidence of Loggable[X].
   * @return the value of x.
   */
  def logLoggable[X: Loggable](prefix: => String)(x: => X): X =
    tee[X](y => loggingFunction(s"$prefix: ${implicitly[Loggable[X]].toLog(y)}"))(x)

  /**
   * Method to generate a log message, pass it to the logFunc, and return the x value.
   * The difference between this method and the logLoggable method is that the value of x will be rendered as a String,
   * simply by invoking toString.
   *
   * @param prefix the message prefix.
   * @param x      the value to be logged and returned.
   * @tparam X the underlying type of x.
   * @return the value of x.
   */
  def logX[X](prefix: => String)(x: => X): X = tee[X](y => loggingFunction(s"$prefix: $y"))(x)

  /**
   * We make this available for any Loggers (such as futureLogger) which might require a LogFunction.
   */
  implicit object LogFunction$ extends LogFunction(loggingFunction.f, true)
}

/**
 * Companion object to Flog.
 */
object Flog {
  /**
   * Method to instantiate a normal instance of Flog with logging via the default logging function (based on the class Flog).
   *
   * @return an instance of Flog.
   */
  def apply(): Flog = Flog(defaultLogFunction[Flog])

  /**
   * Method to instantiate a normal instance of Flog with logging via the given function).
   *
   * @param loggerFunction the LogFunction to use for logging.
   * @return an instance of Flog.
   */
  def apply(loggerFunction: LogFunction): Flog = Flog(loggerFunction, defaultErrorFunction[Flog])

  /**
   * Use this method to create an instance of Flog with the default logging function based on class T.
   *
   * @tparam T the class for which you want to log.
   * @return a new instance of Flog.
   */
  def forClass[T: ClassTag]: Flog = Flog(defaultLogFunction[T], defaultErrorFunction[T])

  /**
   * Use this method to create an instance of Flog with the default logging function based on clazz.
   *
   * @param clazz the class for which you want to log.
   * @return a new instance of Flog.
   */
  def forClass(clazz: Class[_]): Flog = Flog(LogFunction(getDefaultLogger(clazz).debug), LogFunction(getDefaultLogger(clazz).warn))

  /**
   * Method to yield a default logging function (uses LoggerFactory.getLogger) for the class T.
   *
   * @tparam T the class with which the logging messages should be associated.
   * @return a LogFunction.
   */
  def defaultLogFunction[T: ClassTag]: LogFunction = LogFunction(getDefaultLogger.debug)

  /**
   * Method to yield a default error logging function (uses LoggerFactory.getLogger) for the class T.
   *
   * @tparam T the class with which the logging messages should be associated.
   * @return a LogFunction.
   */
  def defaultErrorFunction[T: ClassTag]: LogFunction = LogFunction(getDefaultLogger.warn)

  /**
   * Method which, as a side-effect, invokes function f on the given value of x.
   * Then returns the value of x.
   * If an exception is thrown evaluating f(xx), it is logged as a warning.
   * However, it is possible that the exception was thrown evaluating x,
   * in which case the tee method will throw the same exception.
   * There is nothing to be done about this, of course, but at least there will be a log entry.
   *
   * @param f the function to invoke on x.
   * @param x the value of x.
   * @tparam X the type of x (and the result).
   * @return x.
   */
  def tee[X](f: X => Unit)(x: => X): X = {
    lazy val xx: X = x
    Try(f(xx)) match {
      case Failure(e) => getDefaultLogger[Flog].warn("Exception thrown in tee function", e)
      case _ =>
    }
    xx
  }

  /**
   * Get the default logger from LoggerFactory that is associated with the given class.
   *
   * @tparam T the class to be associated with logging.
   * @return a Logger.
   */
  private def getDefaultLogger[T](implicit classTag: ClassTag[T]): Logger = LoggerFactory.getLogger(classTag.runtimeClass)

  /**
   * Get the default logger from LoggerFactory that is associated with the given clazz.
   *
   * @param clazz the class to be associated with logging.
   * @return a Logger.
   */
  private def getDefaultLogger(clazz: Class[_]): Logger = LoggerFactory.getLogger(clazz)
}

/**
 * LogFunction which is a function of String => Unit (except that the String parameter is defined to be call-by-name).
 * NOTE: the type of f must be (String => Any) because many suitable functions will in fact yield a non-Unit result.
 *
 * @param f a function which takes a String and returns any type.
 *          It is expected that f causes some side-effect such as writing to a log file.
 *          The actual result of invoking f is ignored.
 */
case class LogFunction(f: String => Any, enabled: Boolean = true) {
  /**
   * Apply method for LogFunction which processes the given string w provided that enabled is true.
   *
   * @param w a String to be logged.
   */
  def apply(w: => String): Unit = if (enabled) f(w)

  /**
   * Method to instantiate a new LogFunction with the same value of f, but with enabled set to false.
   *
   * @return a LogFunction which does nothing.
   */
  def disable: LogFunction = LogFunction(f, enabled = false)
}

object LogFunction {
  val noop: LogFunction = LogFunction(_ => ()).disable
}

case class LoggedException(e: Throwable) extends Exception("The cause of this exception has already been logged", e)
