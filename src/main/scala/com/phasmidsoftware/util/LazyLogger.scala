/*
 * Copyright (c) 2019. Phasmid Software
 */

package com.phasmidsoftware.util

import org.slf4j.Logger

trait LazyLogger[L <: Logger] {
  def lazyTrace(l: L)(msg: => String): Unit = if (l.isTraceEnabled) l.trace(msg)

  def lazyDebug(l: L)(msg: => String): Unit = if (l.isDebugEnabled) l.debug(msg)

  def lazyInfo(l: L)(msg: => String): Unit = if (l.isInfoEnabled) l.info(msg)
}

object LazyLogger {

  implicit object LazyLoggerSlf4j extends LazyLogger[Logger]

}
