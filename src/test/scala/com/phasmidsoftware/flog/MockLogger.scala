/*
 * Copyright (c) 2019. Phasmid Software
 */

package com.phasmidsoftware.flog

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

  def debug(format: String, arg: scala.Any) = doLogX

  def debug(format: String, arg1: scala.Any, arg2: scala.Any) = doLogX

  def debug(format: String, arguments: AnyRef*) = doLogX

  def debug(msg: String, t: Throwable) = doLogX

  def debug(marker: Marker, msg: String) = doLogX

  def debug(marker: Marker, format: String, arg: scala.Any) = doLogX

  def debug(marker: Marker, format: String, arg1: scala.Any, arg2: scala.Any) = doLogX

  def debug(marker: Marker, format: String, arguments: AnyRef*) = doLogX

  def debug(marker: Marker, msg: String, t: Throwable) = doLogX

  def isWarnEnabled = level == "WARN" || isInfoEnabled

  def isWarnEnabled(marker: Marker) = false

  def error(msg: String) = doLog(msg)

  def error(format: String, arg: scala.Any) = doLogX

  def error(format: String, arg1: scala.Any, arg2: scala.Any) = doLogX

  def error(format: String, arguments: AnyRef*) = doLogX

  def error(msg: String, t: Throwable) = doLogX

  def error(marker: Marker, msg: String) = doLogX

  def error(marker: Marker, format: String, arg: scala.Any) = doLogX

  def error(marker: Marker, format: String, arg1: scala.Any, arg2: scala.Any) = doLogX

  def error(marker: Marker, format: String, arguments: AnyRef*) = doLogX

  def error(marker: Marker, msg: String, t: Throwable) = doLogX

  def warn(format: String, arg: scala.Any) = doLogX

  def warn(format: String, arguments: AnyRef*) = doLogX

  def warn(format: String, arg1: scala.Any, arg2: scala.Any) = doLogX

  def warn(marker: Marker, msg: String) = doLogX

  def warn(marker: Marker, format: String, arg: scala.Any) = doLogX

  def warn(marker: Marker, format: String, arg1: scala.Any, arg2: scala.Any) = doLogX

  def warn(marker: Marker, format: String, arguments: AnyRef*) = doLogX

  def warn(marker: Marker, msg: String, t: Throwable) = doLogX

  def trace(msg: String) = doLog(msg)

  def trace(format: String, arg: scala.Any) = doLogX

  def trace(format: String, arg1: scala.Any, arg2: scala.Any) = doLogX

  def trace(format: String, arguments: AnyRef*) = doLogX

  def trace(msg: String, t: Throwable) = doLogX

  def trace(marker: Marker, msg: String) = doLogX

  def trace(marker: Marker, format: String, arg: scala.Any) = doLogX

  def trace(marker: Marker, format: String, arg1: scala.Any, arg2: scala.Any) = doLogX

  def trace(marker: Marker, format: String, argArray: AnyRef*) = doLogX

  def trace(marker: Marker, msg: String, t: Throwable) = doLogX

  def isInfoEnabled = level == "INFO" || isDebugEnabled

  def isInfoEnabled(marker: Marker) = true

  def isErrorEnabled = level == "ERROR" || isWarnEnabled

  def isErrorEnabled(marker: Marker) = false

  def isTraceEnabled = level == "TRACE"

  def isTraceEnabled(marker: Marker) = false

  def isDebugEnabled = level == "DEBUG" || isTraceEnabled

  def isDebugEnabled(marker: Marker) = false

  def info(format: String, arg: scala.Any) = doLogX

  def info(format: String, arg1: scala.Any, arg2: scala.Any) = doLogX

  def info(format: String, arguments: AnyRef*) = doLogX

  def info(msg: String, t: Throwable) = doLogX

  def info(marker: Marker, msg: String) = doLogX

  def info(marker: Marker, format: String, arg: scala.Any) = doLogX

  def info(marker: Marker, format: String, arg1: scala.Any, arg2: scala.Any) = doLogX

  def info(marker: Marker, format: String, arguments: AnyRef*) = doLogX

  def info(marker: Marker, msg: String, t: Throwable) = doLogX

  private def doLog(msg: String, e: Throwable = null) = {
    sb.append(s"$name: $level: $msg")
    if (e != null) sb.append(s" threw an exception: ${e.getLocalizedMessage}")
    sb.append("\n")
  }

  private def doLogX: Unit = {}
}
