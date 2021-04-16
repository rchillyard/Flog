/*
 * Copyright (c) 2019. Phasmid Software
 */

package com.phasmidsoftware.flog

import com.phasmidsoftware.flog.Flog.defaultLogFunction
import org.slf4j.LoggerFactory
import scala.reflect.ClassTag

/**
 * Simple functional logging class.
 *
 * Here are the steps you need to follow to enable logging:
 *
 * <ol>
 * <li>Instantiate an instance of Flog, specifying the two parameter values:
 * <dl>
 * <dt>loggingFunction</dt><dd>by default, this will be a function which uses the debug method of a logger based on ;</dd>
 * </dl>
 * Assuming that you named the variable "flog", then "import flog._"</li>
 * <li>Create a String which will form the log message, follow it with "!!" and follow that with the expression you want to log.</li>
 * <li>Most of the time, this is all you need to do.</li>
 * <li>If you wish to override the logging function, then declare something like the following:
 * <code>implicit def logFunc(w: String): LogFunction = LogFunction(p-r-i-n-t-l-n)</code> (take out the dashes, obviously).
 * Create a new Flog instance by using the method withLogFunction(logFunc).
 * Import as before.
 * Follow this with !! and the expression, as usual.
 * </li>
 * <li>If you wish to override the default LogFunction, for example to log according to a particular class,
 * there is a method to give you a default log function based on a specific class (MyClass in this example):
 * <code>Flog.forClass(Flog.defaultLogFunction[MyClass])</code>
 * </li>
 * </ol>
 * <p/>
 * <p>
 * There are several ways to turn logging off (temporarily or permanently) once you've added the log expressions:
 * <dl>
 * <dt>(1A)</dt> <dd>replace the !! method with the |! method for each expression you wish to silence;</dd>
 * <dt>(2)</dt> <dd>use a disabled Flog with flog.disabled (silences all flogging everywhere);</dd>
 * <dt>(3)</dt> <dd>remove the !! expressions.</dd>
 * </dl>
 *
 * @param loggingFunction the LogFunction which is to be used by this Flog.
 */
case class Flog(loggingFunction: LogFunction) {

    /**
     * Implicit class to implement functional logging.
     *
     * If you are using the default logging function (Flog.loggingFunction), then you can instantiate and utilize
     * a new instance of Flogger simply by applying the "!!" operator to a String.
     * However, if you are using a locally defined value of the logging function, you will have to instantiate a Flogger
     * explicitly (see FlogSpec for examples).
     *
     * @param message the message itself which will be evaluated only if enabled is actually turned on.
   */
  implicit class Flogger(message: => String) {
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
     * Method to generate a log entry for an Iterable of a (Loggable) X.
     * Logging is performed as a side effect.
     * Rendering of the x value is via the toLog method of the implicit Loggable[X].
     *
     * @param x the Iterable value to be logged.
     * @tparam X the underlying type of x, which must provide implicit evidence of being Loggable.
     * @return the value of x.
     */
    def !![X: Loggable](x: => Iterable[X]): Iterable[X] = {
      logLoggable(message)(new Loggables {}.seqLoggable[String].toLog((x map (implicitly[Loggable[X]].toLog(_))).toSeq))
      x
    }

    /**
     * Method to generate a log entry for an Option of a (Loggable) X.
     * Logging is performed as a side effect.
     * Rendering of the x value is via the toLog method of the implicit Loggable[X].
     *
     * @param x the optional value to be logged.
     * @tparam X the underlying type of x, which must provide implicit evidence of being Loggable.
     * @return the value of x.
     */
    def !![X: Loggable](x: => Option[X]): Option[X] = {
        logLoggable(message)(new Loggables {}.optionLoggable[String].toLog(x map (implicitly[Loggable[X]].toLog(_))))
      x
    }

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
  }

    /**
     * Use this method to create a new Flog based on this but with the default logging function based on class T.
     *
     * @tparam T the class for which you want to log.
     * @return a new instance of Flog.
     */
    def forClass[T: ClassTag]: Flog = Flog(defaultLogFunction)

    /**
     * Use this method to create a new Flog based on the given logging function.
     *
     * @param logFunc an instance of LogFunction.
     * @return a new instance of Flog.
     */
    def withLogFunction(logFunc: LogFunction): Flog = Flog(logFunc)

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
    def logLoggable[X: Loggable](prefix: => String)(x: => X): X = Flog.tee[X](y => loggingFunction(s"Flog: $prefix: ${implicitly[Loggable[X]].toLog(y)}"))(x)

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
    def logX[X](prefix: => String)(x: => X): X = Flog.tee[X](y => loggingFunction(s"Flog: $prefix: $y"))(x)

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
     * Method to yield a default logging function (uses LoggerFactory.getLogger) for the class T.
     *
     * @tparam T the class with which the logging messages should be associated.
     * @return a LogFunction.
     */
    def defaultLogFunction[T: ClassTag]: LogFunction = LogFunction(LoggerFactory.getLogger(implicitly[ClassTag[T]].runtimeClass).debug)

    /**
     * Method which, as a side-effect, invokes function f on the given value of x.
     * Then returns the value of x.
     * CONSIDER making this less complex!
     *
     * @param f the function to invoke on x.
     * @param x the value of x.
     * @tparam X the type of x (and the result).
     * @return x
     */
    def tee[X](f: X => Unit)(x: => X): X = {
        lazy val xx: X = x
        f(xx)
        xx
    }
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
