/*
 * Copyright (c) 2021. Phasmid Software
 */

package com.phasmidsoftware.flog

import org.slf4j.{Logger, Marker}

import scala.reflect.ClassTag

/**
 * MockLogger for unit testing log messages
 *
 * TODO implement all of the unimplemented stuff
 *
 * @param name     the name of this logger
 * @param logLevel the level of this logger
 * @param sb       a StringBuilder to which we append messages
 */
//noinspection TypeAnnotation,NotImplementedCode
case class MockLogger(name: String, logLevel: String = "DEBUG", sb: StringBuilder = new StringBuilder()) extends Logger {

  def clear(): Unit = {
    sb.clear()
  }

  override def toString: String = sb.toString

  def getName = name

  def debug(msg: String) = doLog(msg, "DEBUG")

  def info(msg: String) = doLog(msg, "INFO")

  def warn(msg: String) = doLog(msg, "WARN")

  def warn(msg: String, t: Throwable) = doLog(msg, "WARN", t)

  def debug(format: String, arg: scala.Any) = doLogX()

  def debug(format: String, arg1: scala.Any, arg2: scala.Any) = doLogX()

  def debug(format: String, arguments: AnyRef*) = doLogX()

  def debug(msg: String, t: Throwable) = doLogX()

  def debug(marker: Marker, msg: String) = doLogX()

  def debug(marker: Marker, format: String, arg: scala.Any) = doLogX()

  def debug(marker: Marker, format: String, arg1: scala.Any, arg2: scala.Any) = doLogX()

  def debug(marker: Marker, format: String, arguments: AnyRef*) = doLogX()

  def debug(marker: Marker, msg: String, t: Throwable) = doLogX()

  def isWarnEnabled = logLevel == "WARN" || isInfoEnabled

  def isWarnEnabled(marker: Marker) = false

  def error(msg: String) = doLog(msg, "ERROR")

  def error(format: String, arg: scala.Any) = doLogX()

  def error(format: String, arg1: scala.Any, arg2: scala.Any) = doLogX()

  def error(format: String, arguments: AnyRef*) = doLogX()

  def error(msg: String, t: Throwable) = doLog(msg, "ERROR", t)

  def error(marker: Marker, msg: String) = doLogX()

  def error(marker: Marker, format: String, arg: scala.Any) = doLogX()

  def error(marker: Marker, format: String, arg1: scala.Any, arg2: scala.Any) = doLogX()

  def error(marker: Marker, format: String, arguments: AnyRef*) = doLogX()

  def error(marker: Marker, msg: String, t: Throwable) = doLogX()

  def warn(format: String, arg: scala.Any) = doLogX()

  def warn(format: String, arguments: AnyRef*) = doLogX()

  def warn(format: String, arg1: scala.Any, arg2: scala.Any) = doLogX()

  def warn(marker: Marker, msg: String) = doLogX()

  def warn(marker: Marker, format: String, arg: scala.Any) = doLogX()

  def warn(marker: Marker, format: String, arg1: scala.Any, arg2: scala.Any) = doLogX()

  def warn(marker: Marker, format: String, arguments: AnyRef*) = doLogX()

  def warn(marker: Marker, msg: String, t: Throwable) = doLogX()

  def trace(msg: String) = doLog(msg, "TRACE")

  def trace(format: String, arg: scala.Any) = doLogX()

  def trace(format: String, arg1: scala.Any, arg2: scala.Any) = doLogX()

  def trace(format: String, arguments: AnyRef*) = doLogX()

  def trace(msg: String, t: Throwable) = doLogX()

  def trace(marker: Marker, msg: String) = doLogX()

  def trace(marker: Marker, format: String, arg: scala.Any) = doLogX()

  def trace(marker: Marker, format: String, arg1: scala.Any, arg2: scala.Any) = doLogX()

  def trace(marker: Marker, format: String, argArray: AnyRef*) = doLogX()

  def trace(marker: Marker, msg: String, t: Throwable) = doLogX()

  def isInfoEnabled = logLevel == "INFO" || isDebugEnabled

  def isInfoEnabled(marker: Marker) = true

  def isErrorEnabled = true

  def isErrorEnabled(marker: Marker) = false

  def isTraceEnabled = logLevel == "TRACE"

  def isTraceEnabled(marker: Marker) = false

  def isDebugEnabled = logLevel == "DEBUG" || isTraceEnabled

  def isDebugEnabled(marker: Marker) = false

  def info(format: String, arg: scala.Any) = doLogX()

  def info(format: String, arg1: scala.Any, arg2: scala.Any) = doLogX()

  def info(format: String, arguments: AnyRef*) = doLogX()

  def info(msg: String, t: Throwable) = doLogX()

  def info(marker: Marker, msg: String) = doLogX()

  def info(marker: Marker, format: String, arg: scala.Any) = doLogX()

  def info(marker: Marker, format: String, arg1: scala.Any, arg2: scala.Any) = doLogX()

  def info(marker: Marker, format: String, arguments: AnyRef*) = doLogX()

  def info(marker: Marker, msg: String, t: Throwable) = doLogX()

  private def doLog(msg: String, level: String, e: Throwable = null): Unit = {
    sb.append(s"$name: $level: $msg")
    if (e != null) sb.append(s" threw an exception: ${e.getLocalizedMessage}")
    sb.append("\n")
    ()
  }

    private def doLogX(): Unit = {}
}

object MockLogger {
    def defaultLogger[T](implicit classTag: ClassTag[T]): MockLogger = defaultLogger(classTag.runtimeClass)

  def defaultLogger(clazz: Class[_]): MockLogger = MockLogger(clazz.toString, "DEBUG", new StringBuilder())
}
