/*
 * Copyright (c) 2021. Phasmid Software
 */

package com.phasmidsoftware.flog

import org.slf4j.LoggerFactory

import java.io.{Flushable, OutputStream, PrintStream, PrintWriter}
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

/**
 * Simple functional logging class.
 *
 * Here are the steps you need to follow to enable logging:
 *
 * <ol>
 * <li>To instantiate a new instance of Flog, specifying the class for which you are logging:
 * simply invoke the apply method: <code>Flog[MyClass]</code>.
 * Assuming that you named the variable "flog", then "import flog._"</li>
 * <li>Create a String which will form the log message, follow it with "!!" (info), or "!?" (debug), or other method,
 * and follow that with the expression you want to log.
 * The construct in this form (including the message and "!!") will yield the value of the expression.</li>
 * <li>Most of the time, this is all you need to do (see README.md for much more information on this).</li>
 * </ol>
 * <p>
 * There are several ways to turn logging off (temporarily or permanently) once you've added the log expressions:
 * <dl>
 * <dt>(0)</dt> <dd>configure the logger (using logback.xml or similar) to ignore the particular log level;</dd>
 * <dt>(1)</dt> <dd>replace the !! method with the |! method for each expression you wish to silence;</dd>
 * <dt>(2)</dt> <dd>use a disabled Flog with flog.disabled (silences all flogging everywhere);</dd>
 * <dt>(3)</dt> <dd>remove the !! expressions.</dd>
 * </dl>
 * </p>
 *
 * @param logger the Logger which is to be used by this Flog.
 */
case class Flog(logger: Logger) extends AutoCloseable {

  import Flog._

  /**
   * Implicit class to implement functional logging.
   *
   * @param message the (call-by-name) message which will be evaluated only any logging takes place.
   */
  implicit class Flogger(message: => String) extends Loggables {
    /**
     * Method to generate an INFO-level log entry for a (Loggable) value of X.
     * Logging is performed as a side effect.
     * Rendering of the x value is via the toLog method of the implicit Loggable[X].
     *
     * @param x the value to be logged.
     * @tparam X the type of x, which must provide implicit evidence of being Loggable.
     * @return the value of x.
     */
    def !![X: Loggable](x: => X): X = info(x)

    /**
     * Method to generate a DEBUG-level log entry for a (Loggable) value of X.
     * Logging is performed as a side effect.
     * Rendering of the x value is via the toLog method of the implicit Loggable[X].
     *
     * @param x the value to be logged.
     * @tparam X the type of x, which must provide implicit evidence of being Loggable.
     * @return the value of x.
     */
    def !?[X: Loggable](x: => X): X = debug(x)

    /**
     * Method to generate a TRACE-level log entry for a (Loggable) value of X.
     * Logging is performed as a side effect.
     * Rendering of the x value is via the toLog method of the implicit Loggable[X].
     *
     * @param x the value to be logged.
     * @tparam X the type of x, which must provide implicit evidence of being Loggable.
     * @return the value of x.
     */
    def !??[X: Loggable](x: => X): X = trace(x)

    /**
     * Synonym for: def !![X: Loggable](x: => X): X
     *
     * Method to generate an INFO-level log entry for a (Loggable) value of X.
     * Logging is performed as a side effect.
     * Rendering of the x value is via the toLog method of the implicit Loggable[X].
     *
     * @param x the value to be logged.
     * @tparam X the type of x, which must provide implicit evidence of being Loggable.
     * @return the value of x.
     */
    def info[X: Loggable](x: => X): X = logLoggable(logger.info)(message)(x)

    /**
     * Synonym for: def !?[X: Loggable](x: => X): X.
     *
     * Method to generate a DEBUG-level log entry for a (Loggable) value of X.
     * Logging is performed as a side effect.
     * Rendering of the x value is via the toLog method of the implicit Loggable[X].
     *
     * @param x the value to be logged.
     * @tparam X the type of x, which must provide implicit evidence of being Loggable.
     * @return the value of x.
     */
    def debug[X: Loggable](x: => X): X = logLoggable(logger.debug)(message)(x)

    /**
     * Synonym for: def !??[X: Loggable](x: => X): X
     *
     * Method to generate a TRACE-level log entry for a (Loggable) value of X.
     * Logging is performed as a side effect.
     * Rendering of the x value is via the toLog method of the implicit Loggable[X].
     *
     * @param x the value to be logged.
     * @tparam X the type of x, which must provide implicit evidence of being Loggable.
     * @return the value of x.
     */
    def trace[X: Loggable](x: => X): X = logLoggable(logger.trace)(message)(x)

    /**
     * Method to generate a WARN-level log entry for a (Loggable) value of X.
     * Logging is performed as a side effect.
     * Rendering of the x value is via the toLog method of the implicit Loggable[X].
     *
     * @param x the value to be logged.
     * @tparam X the type of x, which must provide implicit evidence of being Loggable.
     * @return the value of x.
     */
    def warn[X: Loggable](x: => X): X = logLoggable(logger.warn)(message)(x)

    /**
     * Method to generate an ERROR-level log entry for a (Loggable) value of X.
     * Logging is performed as a side effect.
     * Rendering of the x value is via the toLog method of the implicit Loggable[X].
     *
     * NOTE: there is a null coded here for the default value of t.
     * It's justified because it is simply following the Java calling convention.
     *
     * @param x the value to be logged.
     * @param t a Throwable (defaults to null) -- but don't forget the empty parentheses in that case.
     * @tparam X the type of x, which must provide implicit evidence of being Loggable.
     * @return the value of x.
     */
    def error[X: Loggable](x: => X)(t: Throwable = null): X = logLoggable(w => logger.error(w, t))(message)(x)

    /**
     * Method to generate an info log entry for an Iterable of a (Loggable) X.
     * Logging is performed as a side effect.
     * Rendering of the x value is via the toLog method of the implicit Loggable[X].
     *
     * @param xs the Iterable value to be logged.
     * @tparam X the underlying type of x, which must provide implicit evidence of being Loggable.
     * @return the value of x.
     */
    def !![X: Loggable](xs: => Iterable[X]): Iterable[X] = logIterable(logger.info, xs)

    /**
     * Method to generate an info log entry for an Option of a (Loggable) X.
     * Logging is performed as a side effect.
     * Rendering of the x value is via the toLog method of the implicit Loggable[X].
     *
     * @param xo the optional value to be logged.
     * @tparam X the underlying type of x, which must provide implicit evidence of being Loggable.
     * @return the value of x.
     */
    def !![X: Loggable](xo: => Option[X]): Option[X] = logOption(logger.info, xo)

    /**
     * Method to generate an info log entry for a Map[K, V].
     * Logging is performed as a side effect.
     *
     * @param kVm the Map to be logged.
     * @tparam K the type of the map keys, which must provide implicit evidence of being Loggable.
     * @tparam V the type of the map values, which must provide implicit evidence of being Loggable.
     * @return the value of x.
     */
    def !![K: Loggable, V: Loggable](kVm: => Map[K, V]): Map[K, V] = logMap(logger.info, kVm)

    /**
     * Method to generate an info log entry for a Future[X].
     * Logging is performed as a side effect.
     *
     * @param xf the future value to be logged.
     * @tparam X the underlying type of the given input.
     * @return the value of xf.
     */
    def !![X: Loggable](xf: => Future[X])(implicit ec: ExecutionContext): Future[X] = logFuture(logger.info, xf)

    /**
     * Method to generate a debug log entry for an Iterable of a (Loggable) X.
     * Logging is performed as a side effect.
     * Rendering of the x value is via the toLog method of the implicit Loggable[X].
     *
     * @param xs the Iterable value to be logged.
     * @tparam X the underlying type of x, which must provide implicit evidence of being Loggable.
     * @return the value of x.
     */
    def !?[X: Loggable](xs: => Iterable[X]): Iterable[X] = logIterable(logger.debug, xs)

    /**
     * Method to generate a debug log entry for an Option of a (Loggable) X.
     * Logging is performed as a side effect.
     * Rendering of the x value is via the toLog method of the implicit Loggable[X].
     *
     * @param xo the optional value to be logged.
     * @tparam X the underlying type of x, which must provide implicit evidence of being Loggable.
     * @return the value of x.
     */
    def !?[X: Loggable](xo: => Option[X]): Option[X] = logOption(logger.debug, xo)

    /**
     * Method to generate a trace log entry for a Map[K, V].
     * Logging is performed as a side effect.
     *
     * @param kVm the Map to be logged.
     * @tparam K the type of the map keys, which must provide implicit evidence of being Loggable.
     * @tparam V the type of the map values, which must provide implicit evidence of being Loggable.
     * @return the value of x.
     */
    def !?[K: Loggable, V: Loggable](kVm: => Map[K, V]): Map[K, V] = logMap(logger.trace, kVm)

    /**
     * Method to generate an debug log entry for a Future[X].
     * Logging is performed as a side effect.
     *
     * @param xf the future value to be logged.
     * @tparam X the underlying type of the given input.
     * @return the value of xf.
     */
    def !?[X: Loggable](xf: => Future[X])(implicit ec: ExecutionContext): Future[X] = logFuture(logger.debug, xf)

    /**
     * Method to generate a trace log entry for an Iterable of a (Loggable) X.
     * Logging is performed as a side effect.
     * Rendering of the x value is via the toLog method of the implicit Loggable[X].
     *
     * @param xs the Iterable value to be logged.
     * @tparam X the underlying type of x, which must provide implicit evidence of being Loggable.
     * @return the value of x.
     */
    def !??[X: Loggable](xs: => Iterable[X]): Iterable[X] = logIterable(logger.trace, xs)

    /**
     * Method to generate a trace log entry for an Option of a (Loggable) X.
     * Logging is performed as a side effect.
     * Rendering of the x value is via the toLog method of the implicit Loggable[X].
     *
     * @param xo the optional value to be logged.
     * @tparam X the underlying type of x, which must provide implicit evidence of being Loggable.
     * @return the value of x.
     */
    def !??[X: Loggable](xo: => Option[X]): Option[X] = logOption(logger.trace, xo)

    /**
     * Method to generate a debug log entry for a Map[K, V].
     * Logging is performed as a side effect.
     *
     * @param kVm the Map to be logged.
     * @tparam K the type of the map keys, which must provide implicit evidence of being Loggable.
     * @tparam V the type of the map values, which must provide implicit evidence of being Loggable.
     * @return the value of x.
     */
    def !??[K: Loggable, V: Loggable](kVm: => Map[K, V]): Map[K, V] = logMap(logger.debug, kVm)

    /**
     * Method to generate a trace log entry for a Future[X].
     * Logging is performed as a side effect.
     *
     * @param xf the future value to be logged.
     * @tparam X the underlying type of the given input.
     * @return the value of xf.
     */
    def !??[X: Loggable](xf: => Future[X])(implicit ec: ExecutionContext): Future[X] = logFuture(logger.trace, xf)

    /**
     * Method to generate a log entry for a type which is not itself Loggable.
     * Logging is performed as a side effect.
     * Rendering of the x value is via the toString method.
     *
     * @param x the value to be logged.
     * @tparam X the type of x.
     * @return the value of x.
     */
    def !|[X](x: => X): X = logX(logger.info)(message)(x)

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
      logger.error(s"$message", e)
      Failure(LoggedException(e))
    })

    private def logMap[K: Loggable, V: Loggable](logFunction: LogFunction, kVm: => Map[K, V]): Map[K, V] = {
      implicit val g: Loggable[(K, V)] = kVLoggable
      tee[Map[K, V]](y => logLoggable(logFunction)(message)(toLog(y)))(kVm)
    }

    private def logIterable[X: Loggable](logFunction: LogFunction, xo: => Iterable[X]): Iterable[X] =
      tee[Iterable[X]](y => logLoggable(logFunction)(message)(toLog(y)))(xo)

    private def logOption[X: Loggable](logFunction: LogFunction, xo: => Option[X]): Option[X] =
      tee[Option[X]](y => logLoggable(logFunction)(message)(toLog(y)))(xo)

    private def logFuture[X: Loggable](logFunction: LogFunction, xf: => Future[X])(implicit ec: ExecutionContext): Future[X] = {
      val uuid = java.util.UUID.randomUUID
      implicit val z: Logger = logger
      implicit val xtl: Loggable[Try[X]] = tryLoggable
      xf.onComplete(xy => logLoggable(logFunction)(message)(s"future [$uuid] completed : ${xtl.toLog(xy)}"))
      tee[Future[X]](_ => logLoggable(logFunction)(message)(s"future promise [$uuid] created... "))(xf)
    }

    private def toLog[X: Loggable](y: Option[X]): String = optionLoggable[String].toLog(y map implicitly[Loggable[X]].toLog)

    private def toLog[X: Loggable](y: Iterable[X]): String = iterableLoggable[String]().toLog(y map implicitly[Loggable[X]].toLog)

    private def toLog[K, V](y: Map[K, V])(implicit kVl: Loggable[(K, V)]): String = iterableLoggable[String]("{}").toLog(y map kVl.toLog)
  }

  /**
   * Method to capture any text held by this Flog.
   * In the case of a "standard" slf4j logger, the returned value will is undefined.
   *
   * @return the value of logger.toString.
   */
  override def toString: String = logger.toString

  /**
   * Use this method to create a new Flog based on the given logging function.
   *
   * @param logger an instance of LogFunction.
   * @return a new instance of Flog.
   */
  def withLogger(logger: Logger): Flog = Flog(logger)

  /**
   * Use this method to create a new Flog with logging disabled.
   *
   * @return a new instance of Flog.
   */
  def disabled: Flog = withLogger(Logger.bitBucket)

  /**
   * Close this Flog.
   * For standard slf4j loggers, this does nothing.
   * However, for Appendable loggers, this call will typically flush and close the appendable instance.
   */
  def close(): Unit = logger.close()

  /**
   * Method to generate a log message based on x, pass it to the logFunc, and return the x value.
   * The value of x will be rendered as a String but invoking toLog on the implicit value of Loggable[X].
   *
   * @param prefix the message prefix.
   * @param x      the value to be logged and returned.
   * @tparam X the underlying type of x, which is required to provide evidence of Loggable[X].
   * @return the value of x.
   */
  private def logLoggable[X: Loggable](function: LogFunction)(prefix: => String)(x: => X): X =
    tee[X](y => function(s"$prefix: ${implicitly[Loggable[X]].toLog(y)}"))(x)

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
  private def logX[X](function: LogFunction)(prefix: => String)(x: => X): X = logLoggable(function)(prefix)(x)(new Loggables {}.anyLoggable)
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
  def apply[T: ClassTag]: Flog = Flog(Logger[T])

  /**
   * Use this method to create an instance of Flog with the default logging function based on clazz.
   *
   * @param clazz the class for which you want to log.
   * @return a new instance of Flog.
   */
  def apply(clazz: Class[_]): Flog = Flog(Logger.forClass(clazz))

  /**
   * Method to instantiate a normal instance of Flog with logging via the default logging function (based on the class Flog).
   *
   * @return an instance of Flog.
   */
  def apply(logger: org.slf4j.Logger): Flog = Flog(Logger(logger))

  /**
   * Method to create a Flog from a StringBuilder.
   * Mostly intended for testing.
   *
   * @param sb the StringBuilder.
   * @return an instance of Flog.
   */
  def apply(sb: StringBuilder): Flog = Flog(Logger(sb))

  /**
   * Method to create a Flog from an Appendable, AutoCloseable, Flushable object.
   * Don't forget to close this Flog.
   *
   * @param a the Appendable, which must also be AutoCloseable and Flushable.
   * @return an instance of Flog.
   */
  def apply(a: Appendable with AutoCloseable with Flushable): Flog = Flog(Logger(a))

  /**
   * Method to create a Flog from a PrintStream.
   * NOTE: invokes apply(Appendable).
   *
   * @param s the PrintStream.
   * @return an instance of Flog.
   */
  def apply(s: PrintStream): Flog = Flog(new PrintWriter(s))

  /**
   * Method to create a Flog from an OutputStream.
   * NOTE: invokes apply(Appendable).
   * Don't forget to close this Flog.
   *
   * @param o the OutputStream.
   * @return an instance of Flog.
   */
  def apply(o: OutputStream): Flog = Flog(new PrintWriter(o))

  /**
   * Private method which, as a side-effect, invokes function f on the given value of x,
   * and returns the value of x.
   * If an exception is raised while evaluating f(xx), it is caught and logged as an error.
   * However, it is possible that the exception is thrown while evaluating x,
   * in which case the tee method will throw the same exception.
   * There is nothing to be done about this, of course, but at least there will be a log entry.
   *
   * @param f the function to invoke on x.
   * @param x the value of x.
   * @tparam X the type of x (and the result).
   * @return x.
   */
  private def tee[X](f: X => Unit)(x: => X): X = {
    lazy val xx: X = x
    Try(f(xx)) match {
      case Failure(e) => Logger[Flog].error("Exception thrown in tee function", e)
      case _ =>
    }
    xx
  }
}

/**
 * Trait to represent a Logger.
 */
trait Logger extends AutoCloseable with Flushable {
  /**
   * Method to furnish a LogFunction corresponding to the trace level of logging.
   *
   * @return a LogFunction
   */
  def trace: LogFunction

  /**
   * Method to furnish a LogFunction corresponding to the debug level of logging.
   *
   * @return a LogFunction
   */
  def debug: LogFunction

  /**
   * Method to furnish a LogFunction corresponding to the info level of logging.
   *
   * @return a LogFunction
   */
  def info: LogFunction

  /**
   * Method to furnish a LogFunction corresponding to the warn level of logging.
   *
   * @return a LogFunction
   */
  def warn: LogFunction

  /**
   * Method to furnish a (String, Throwable) -> Unit for dealing with errors in the logs.
   * This default implementation is over-ridden for Slf4J-based loggers.
   *
   * @return a function (String, Throwable) => Unit.
   */
  def error: (String, Throwable) => Unit = (w, e) => {
    System.err.println(s"$w: exception:")
    Option(e) map (t => t.printStackTrace(System.err))
  }

  /**
   * Method to furnish a LogFunction that does nothing and which does not cause evaluation of the message.
   *
   * @return a LogFunction which does nothing.
   */
  def none: LogFunction = LogFunction.bitBucket

  /**
   * Method to flush. By default, this does nothing.
   */
  def flush(): Unit = ()

  /**
   * Method to close. By default, this does nothing.
   */
  def close(): Unit = ()

}

object Logger {

  /**
   * Method to yield an slf4j-based Logger for a particular org.slf4j.Logger.
   *
   * @param slf4jLogger the given org.slf4j.Logger.
   * @return a new instance of Slf4jLogger.
   */
  def apply(slf4jLogger: org.slf4j.Logger): Logger = Slf4jLogger(slf4jLogger)

  /**
   * Method to yield an slf4j-based Logger for a particular class.
   * There's no real reason to use this method directly, when you can use the apply method immediately below.
   *
   * @param clazz the Class for which you require a Logger.
   * @return a new instance of Slf4jLogger.
   */
  def forClass(clazz: Class[_]): Logger = Logger(LoggerFactory.getLogger(clazz))

  /**
   * Normal method for creating an slf4j-based Logger for a particular class.
   *
   * @tparam T the class for which you require a Logger.
   * @return a new instance of Slf4jLogger.
   */
  def apply[T: ClassTag]: Logger = forClass(implicitly[ClassTag[T]].runtimeClass)

  /**
   * Method to create a Logger based on a LogFunction.
   *
   * @param f an instance of LogFunction.
   * @return a new instance of GenericLogger.
   */
  def apply(f: LogFunction): Logger = GenericLogger(f)

  /**
   * Method to create a Logger based on a StringBuilder.
   * This is primarily for testing.
   *
   * @param sb an instance of StringBuilder.
   * @return a new instance of GenericLogger.
   */
  def apply(sb: StringBuilder): Logger = StringBuilderLogger(sb)

  /**
   * Method to create a Logger based on an Appendable, for example PrintStream, or any Writer.
   *
   * @param a an instance of Appendable
   * @return a new instance of GenericLogger.
   */
  def apply(a: Appendable with AutoCloseable with Flushable): Logger = AppendableLogger(a)

  /**
   * Method to create a Logger which does nothing (and does not evaluate the log message).
   *
   * @return a new instance of GenericLogger.
   */
  lazy val bitBucket: Logger = Logger(LogFunction.bitBucket)
}

/**
 * Class to create a Logger based on Slf4j (simple logging facade for Java).
 *
 * @param logger an instance of org.slf4j.Logger.
 */
case class Slf4jLogger(logger: org.slf4j.Logger) extends Logger {
  def trace: LogFunction = LogFunction(w => if (logger.isTraceEnabled) logger.trace(w) else ())

  def debug: LogFunction = LogFunction(w => if (logger.isDebugEnabled()) logger.debug(w) else ())

  def info: LogFunction = LogFunction(w => if (logger.isInfoEnabled) logger.info(w) else ())

  def warn: LogFunction = LogFunction(w => if (logger.isWarnEnabled) logger.warn(w) else ())

  override def error: (String, Throwable) => Unit = (w, x) => if (logger.isErrorEnabled) logger.error(w, x) else super.error(w, x)

  override def toString: String = "<org.slf4j.Logger>"
}

/**
 * Class to represent a Logger which is based on a LogFunction.
 *
 * @param logFunction the LogFunction.
 */
case class GenericLogger(logFunction: LogFunction) extends Logger {
  def trace: LogFunction = logFunction

  def debug: LogFunction = logFunction

  def info: LogFunction = logFunction

  def warn: LogFunction = logFunction
}

/**
 * Class to represent a Logger which is based on an Appendable (which must also be AutoCloseable and Flushable.
 *
 * @param appendable an instance of Appendable with is also AutoCloseable and Flushable.
 */
case class AppendableLogger(appendable: Appendable with AutoCloseable with Flushable) extends Logger {
  def trace: LogFunction = LogFunction(appendable)

  def debug: LogFunction = LogFunction(appendable)

  def info: LogFunction = LogFunction(appendable)

  def warn: LogFunction = LogFunction(appendable)

  /**
   * Method to flush the appendable.
   */
  override def flush(): Unit = appendable.flush()

  /**
   * Method to close the appendable.
   * NOTE: we close it by flushing (and nothing else).
   * TODO figure out why it doesn't work if we try to close it too.
   */
  override def close(): Unit = flush()

  override def toString: String = "<appendable>"
}


/**
 * Class to represent a Logger which is based on a StringBuilder.
 *
 * @param sb an instance of StringBuilder.
 */
case class StringBuilderLogger(sb: StringBuilder) extends Logger {
  def trace: LogFunction = LogFunction(sb)

  def debug: LogFunction = LogFunction(sb)

  def info: LogFunction = LogFunction(sb)

  def warn: LogFunction = LogFunction(sb)

  /**
   * Method to furnish a (String, Throwable) -> Unit for dealing with errors in the logs.
   * This default implementation is over-ridden for Slf4J-based loggers.
   *
   * NOTE: there is a null coded here for the situation where error is called without a Throwable.
   * It's justified because it is simply following the Java calling convention.
   *
   * @return a function (String, Throwable) => Unit.
   */
  override def error: (String, Throwable) => Unit = {
    case (s, null) => sb.append(s"$s: ERROR\n")
    case (s, x) => sb.append(s"$s: ERROR: ${x.getLocalizedMessage}\n")
  }

  /**
   * Method to flush the appendable.
   */
  override def flush(): Unit = ()

  /**
   * Method to close the appendable.
   * NOTE: we close it by flushing (and nothing else).
   * TODO figure out why it doesn't work if we try to close it too.
   */
  override def close(): Unit = flush()

  override def toString: String = sb.toString()
}

/**
 * LogFunction which, almost, extends String => Unit.
 * BUT, and this is a big but, if you simply write type LogFunction = String => Unit,
 * you won't get the benefit of call-by-name parameter handling.
 */
trait LogFunction {
  /**
   * Apply method which processes the given call-by-name string.
   *
   * @param w a String to be logged.
   */
  def apply(w: => String): Unit
}

/**
 * LogFunction which is a function of String => Unit (except that the String parameter is defined to be call-by-name).
 * NOTE: the type of f must be (String => Any) because many suitable functions will in fact yield a non-Unit result.
 *
 * @param f a function which takes a String and returns any type.
 *          It is expected that f causes some side-effect such as writing to a log file.
 *          The actual result of invoking f is ignored.
 */
case class GenericLogFunction(f: String => Any, enabled: Boolean = true) extends LogFunction {
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
  def disable: LogFunction = GenericLogFunction(f, enabled = false)
}

object LogFunction {
  /**
   * Method to create a LogFunction based on a function String => Any.
   *
   * @param f the given function.
   * @return a new instance of LogFunction.
   */
  def apply(f: String => Any): LogFunction = GenericLogFunction(f)

  /**
   * Method to create a LogFunction based on a StringBuilder.
   * This is primarily for testing.
   *
   * @param sb an instance of StringBuilder.
   * @return a new instance of GenericLogFunction.
   */
  def apply(sb: StringBuilder): LogFunction = GenericLogFunction{s => sb.append(s); sb.append("\n")}

  /**
   * Method to create a LogFunction based on an Appendable, such as PrintStream, Writer.
   *
   * @param a an instance of Appendable.
   * @return a new instance of GenericLogFunction.
   */
  def apply(a: Appendable): LogFunction = GenericLogFunction{s => a.append(s); a.append("\n")}

  /**
   * A LogFunction which does nothing (and does not evaluate the log message).
   */
  lazy val bitBucket: LogFunction = GenericLogFunction(_ => ()).disable
}

case class LoggedException(e: Throwable) extends Exception("The cause of this exception has already been logged", e)
