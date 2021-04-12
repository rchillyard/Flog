/*
 * Copyright (c) 2019. Phasmid Software
 */

package com.phasmidsoftware.util

/**
 * A type class to enable objects to be rendered laconically as Strings for the purpose of logging.
 *
 * @tparam T the underlying type of the Loggable.
 */
trait Loggable[T] {

  /**
   * Generate a compact String without any newlines for t.
   *
   * @param t the given value to log.
   * @return a String without newlines.
   */
  def toLog(t: T): String
}

object Loggable {

  trait LoggableBoolean extends Loggable[Boolean] {
    def toLog(t: Boolean): String = t.toString
  }

  implicit object LoggableBoolean extends LoggableBoolean

  trait LoggableByte extends Loggable[Byte] {
    def toLog(t: Byte): String = t.toString
  }

  implicit object LoggableByte extends LoggableByte

  trait LoggableShort extends Loggable[Short] {
    def toLog(t: Short): String = t.toString
  }

  implicit object LoggableShort extends LoggableShort

  trait LoggableInt extends Loggable[Int] {
    def toLog(t: Int): String = t.toString
  }

  implicit object LoggableInt extends LoggableInt

  trait LoggableLong extends Loggable[Long] {
    def toLog(t: Long): String = t.toString
  }

  implicit object LoggableLong extends LoggableLong

  trait LoggableBigInt extends Loggable[BigInt] {
    def toLog(t: BigInt): String = t.toString
  }

  implicit object LoggableBigInt extends LoggableBigInt

  trait LoggableString extends Loggable[String] {
    def toLog(t: String): String = t
  }

  implicit object LoggableString extends LoggableString

  trait LoggableDouble extends Loggable[Double] {
    def toLog(t: Double): String = t.toString
  }

  implicit object LoggableDouble extends LoggableDouble

  implicit object LoggableBigDecimal extends LoggableBigDecimal

  trait LoggableBigDecimal extends Loggable[BigDecimal] {
    def toLog(t: BigDecimal): String = t.toString
  }

  /**
   * CONSIDER replacing this with a reference to on of the Loggables instead.
   *
   * @tparam X the underlying type of the Iterable to be logged.
   */
  trait LoggableIterable[X] extends Loggable[Iterable[X]] {
    def toLog(t: Iterable[X]): String = t.toString()
  }

  implicit object LoggableIterableAny extends LoggableIterable[Any]

  trait LoggableUnit extends Loggable[Unit] {
    def toLog(t: Unit): String = ""
  }

  implicit object LoggableUnit extends LoggableUnit
}