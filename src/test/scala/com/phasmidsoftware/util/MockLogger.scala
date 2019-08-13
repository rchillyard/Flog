/*
 * Copyright (c) 2019. Phasmid Software
 */

package com.phasmidsoftware.util

import org.slf4j.{Logger, Marker}

/**
 * MockLogger for unit testing log messages
 *
 * TODO implement all of the unimplemented stuff
 *
 * @param name  the name of this logger
 * @param level the level of this logger
 * @param sb    a StringBuilder to which we append messages
 */
//noinspection TypeAnnotation,NotImplementedCode
case class MockLogger(name: String, level: String = "DEBUG", sb: StringBuilder = new StringBuilder()) extends Logger {

  def clear(): Unit = {
    sb.clear()
  }

  override def toString: String = sb.toString

  def getName = name

  def debug(msg: String) = doLog(msg)

  def info(msg: String) = doLog(msg)

  def warn(msg: String) = doLog(msg)

  def warn(msg: String, t: Throwable) = doLog(msg, t)

  private def doLog(msg: String, e: Throwable = null) = {
    sb.append(s"$name: $level: $msg")
    if (e != null) sb.append(s" threw an exception: ${e.getLocalizedMessage}")
    sb.append("\n")
  }

  def debug(format: String, arg: scala.Any) = ???

  def debug(format: String, arg1: scala.Any, arg2: scala.Any) = ???

  def debug(format: String, arguments: AnyRef*) = ???

  def debug(msg: String, t: Throwable) = ???

  def debug(marker: Marker, msg: String) = ???

  def debug(marker: Marker, format: String, arg: scala.Any) = ???

  def debug(marker: Marker, format: String, arg1: scala.Any, arg2: scala.Any) = ???

  def debug(marker: Marker, format: String, arguments: AnyRef*) = ???

  def debug(marker: Marker, msg: String, t: Throwable) = ???

  def isWarnEnabled = level == "WARN" || isInfoEnabled

  def isWarnEnabled(marker: Marker) = ???

  def error(msg: String) = doLog(msg)

  def error(format: String, arg: scala.Any) = ???

  def error(format: String, arg1: scala.Any, arg2: scala.Any) = ???

  def error(format: String, arguments: AnyRef*) = ???

  def error(msg: String, t: Throwable) = ???

  def error(marker: Marker, msg: String) = ???

  def error(marker: Marker, format: String, arg: scala.Any) = ???

  def error(marker: Marker, format: String, arg1: scala.Any, arg2: scala.Any) = ???

  def error(marker: Marker, format: String, arguments: AnyRef*) = ???

  def error(marker: Marker, msg: String, t: Throwable) = ???

  def warn(format: String, arg: scala.Any) = ???

  def warn(format: String, arguments: AnyRef*) = ???

  def warn(format: String, arg1: scala.Any, arg2: scala.Any) = ???

  def warn(marker: Marker, msg: String) = ???

  def warn(marker: Marker, format: String, arg: scala.Any) = ???

  def warn(marker: Marker, format: String, arg1: scala.Any, arg2: scala.Any) = ???

  def warn(marker: Marker, format: String, arguments: AnyRef*) = ???

  def warn(marker: Marker, msg: String, t: Throwable) = ???

  def trace(msg: String) = doLog(msg)

  def trace(format: String, arg: scala.Any) = ???

  def trace(format: String, arg1: scala.Any, arg2: scala.Any) = ???

  def trace(format: String, arguments: AnyRef*) = ???

  def trace(msg: String, t: Throwable) = ???

  def trace(marker: Marker, msg: String) = ???

  def trace(marker: Marker, format: String, arg: scala.Any) = ???

  def trace(marker: Marker, format: String, arg1: scala.Any, arg2: scala.Any) = ???

  def trace(marker: Marker, format: String, argArray: AnyRef*) = ???

  def trace(marker: Marker, msg: String, t: Throwable) = ???

  def isInfoEnabled = level == "INFO" || isDebugEnabled

  def isInfoEnabled(marker: Marker) = ???

  def isErrorEnabled = level == "ERROR" || isWarnEnabled

  def isErrorEnabled(marker: Marker) = ???

  def isTraceEnabled = level == "TRACE"

  def isTraceEnabled(marker: Marker) = ???

  def isDebugEnabled = level == "DEBUG" || isTraceEnabled

  def isDebugEnabled(marker: Marker) = ???

  def info(format: String, arg: scala.Any) = ???

  def info(format: String, arg1: scala.Any, arg2: scala.Any) = ???

  def info(format: String, arguments: AnyRef*) = ???

  def info(msg: String, t: Throwable) = ???

  def info(marker: Marker, msg: String) = ???

  def info(marker: Marker, format: String, arg: scala.Any) = ???

  def info(marker: Marker, format: String, arg1: scala.Any, arg2: scala.Any) = ???

  def info(marker: Marker, format: String, arguments: AnyRef*) = ???

  def info(marker: Marker, msg: String, t: Throwable) = ???
}
