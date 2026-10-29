/*
 * Copyright (c) 2021. Phasmid Software
 */

package com.phasmidsoftware.flog

import java.time.temporal.Temporal
import scala.annotation.implicitNotFound

/**
 * A type class to enable objects to be rendered laconically as Strings for the purpose of logging.
 *
 * @tparam T the underlying type of the Loggable.
 */
@implicitNotFound(msg = "Cannot find an implicit instance of Loggable[${T}]. Typically, you should invoke a suitable method from Loggables.")
trait Loggable[-T] {

  /**
   * Generate a compact String without any newlines for t.
   *
   * @param t the given value to log.
   * @return a String without newlines.
   */
  def toLog(t: T): String
}

/**
 * Companion object to Loggable.
 * This is where the compiler can find the various given instances of Loggable[T].
 */
object Loggable {

  /**
   * Method to yield a Loggable[T] which depends only on the toString method.
   * This method will be invoked explicitly where no other Loggable[T] is in scope.
   * NOTE: kept as a plain def (not a given) so it can be passed explicitly via `using`.
   *
   * @tparam T the underlying type of the required Loggable.
   * @return a Loggable[T].
   */
  def loggableAny[T]: Loggable[T] = (t: Any) => t.toString

  /**
   * Given instance of Loggable[Unit]. Returns an empty string for any Unit value.
   */
  given Loggable[Unit] with {
    def toLog(t: Unit): String = ""
  }

  /**
   * Given instance of Loggable[Boolean]. Renders via toString.
   */
  given Loggable[Boolean] with {
    def toLog(t: Boolean): String = t.toString
  }

  /**
   * Given instance of Loggable[Byte]. Renders via toString.
   */
  given Loggable[Byte] with {
    def toLog(t: Byte): String = t.toString
  }

  /**
   * Given instance of Loggable[Short]. Renders via toString.
   */
  given Loggable[Short] with {
    def toLog(t: Short): String = t.toString
  }

  /**
   * Given instance of Loggable[Int]. Renders via toString.
   */
  given Loggable[Int] with {
    def toLog(t: Int): String = t.toString
  }

  /**
   * Given instance of Loggable[Long]. Renders via toString.
   */
  given Loggable[Long] with {
    def toLog(t: Long): String = t.toString
  }

  /**
   * Given instance of Loggable[BigInt]. Renders via toString.
   */
  given Loggable[BigInt] with {
    def toLog(t: BigInt): String = t.toString
  }

  /**
   * Given instance of Loggable[String]. Returns the string itself.
   */
  given Loggable[String] with {
    def toLog(t: String): String = t
  }

  /**
   * Given instance of Loggable[Double]. Renders via toString.
   */
  given Loggable[Double] with {
    def toLog(t: Double): String = t.toString
  }

  /**
   * Given instance of Loggable[BigDecimal]. Renders via toString.
   */
  given Loggable[BigDecimal] with {
    def toLog(t: BigDecimal): String = t.toString
  }

  /**
   * Given instance of Loggable[Temporal]. Renders via toString.
   */
  given Loggable[Temporal] with {
    def toLog(t: Temporal): String = t.toString
  }

  /**
   * An abstract class that provides logging capability for `Option` values by extending
   * the `Loggable` type class for `Option[T]`.
   *
   * @tparam T the underlying type contained within the `Option`.
   * @param evidence a context parameter ensuring a `Loggable` instance exists for type `T`.
   */
  abstract class LoggableOption[T](using evidence: Loggable[T]) extends Loggable[Option[T]] {
    private val z: Loggable[Option[T]] = new Loggables {}.optionLoggable

    /**
     * Converts the given `Option` value to its loggable string representation.
     *
     * @param t the `Option` value to be logged.
     * @return a string representation of the `Option` value.
     */
    def toLog(t: Option[T]): String = z.toLog(t)
  }

  /**
   * Given instance of Loggable[Option[Int]].
   */
  given LoggableOption[Int] with {}

  /**
   * Given instance of Loggable[Option[String]].
   */
  given LoggableOption[String] with {}

  /**
   * Given instance of Loggable[Map[K, V]] for any K and V that are themselves Loggable.
   */
  given loggableMap[K: Loggable, V: Loggable]: Loggable[Map[K, V]] =
  new Loggables {}.mapLoggable[K, V]()

  /**
   * Given instance of Loggable[Option[Map[K, V]]] for any K and V that are themselves Loggable.
   */
  given loggableOptionMap[K: Loggable, V: Loggable]: Loggable[Option[Map[K, V]]] = {
    given mapLog: Loggable[Map[K, V]] = new Loggables {}.mapLoggable[K, V]()
    new Loggables {}.optionLoggable[Map[K, V]]
  }

}