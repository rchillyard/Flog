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
 * This is where the compiler can find the various implicit values of Loggable[T].
 */
object Loggable {

  /**
   * Implicit method to yield a Loggable[T] which depends only on the toString method.
   * This method will be invoked to produce a Loggable[T] if no other (implicit) Loggable[T] is in scope.
   *
   * @tparam T the underlying type of the required Loggable.
   * @return a Loggable[T].
   */
  def loggableAny[T]: Loggable[T] = (t: Any) => t.toString

  /**
   * A Loggable implementation for the `Unit` type.
   *
   * This trait provides a default implementation of the method `toLog`
   * for the `Unit` type, which always returns an empty string.
   * It is primarily used to allow `Unit` to participate in logging,
   * adhering to the `Loggable` type class contract.
   */
  trait LoggableUnit extends Loggable[Unit] {
    /**
     * Converts a value of type `Unit` to its corresponding loggable string representation.
     *
     * @param t the `Unit` value to be logged
     * @return an empty string as the log representation of `Unit`
     */
    def toLog(t: Unit): String = ""
  }

  /**
   * An implicit object that provides a default `Loggable` implementation for the `Unit` type.
   *
   * This object enables the `Unit` type to conform to the `Loggable` type class by
   * providing an empty string as the log representation. It is useful for scenarios
   * where logging requires a `Unit` data type to be handled consistently.
   */
  implicit object LoggableUnit extends LoggableUnit

  /**
   * A trait that provides a loggable representation for Boolean values.
   * It extends the `Loggable` type class, enabling Boolean values to be
   * rendered as compact Strings for logging purposes.
   */
  trait LoggableBoolean extends Loggable[Boolean] {
    /**
     * Converts a Boolean value into its String representation for logging purposes.
     *
     * @param t the Boolean value to be converted
     * @return the String representation of the Boolean value
     */
    def toLog(t: Boolean): String = t.toString
  }

  /**
   * An implicit object extending LoggableBoolean to provide a Loggable implementation for Boolean values.
   * This enables automatic conversion of Boolean values into their string representation for logging purposes.
   */
  implicit object LoggableBoolean extends LoggableBoolean

  /**
   * An implementation of the `Loggable` trait for the `Byte` type, providing
   * a mechanism to render a `Byte` as a compact string representation for logging purposes.
   *
   * This trait supplies a default implementation of the `toLog` method, which
   * converts a `Byte` to its string representation through `toString`.
   */
  trait LoggableByte extends Loggable[Byte] {
    /**
     * Converts a `Byte` value to its string representation for logging purposes.
     *
     * @param t the `Byte` value to be converted to a string
     * @return the string representation of the given `Byte`
     */
    def toLog(t: Byte): String = t.toString
  }

  /**
   * Implicit object providing a `Loggable` instance for `Byte` values.
   *
   * Converts `Byte` values into their `String` representation for logging purposes.
   * Inherits the functionality from the `Loggable[Byte]` trait.
   */
  implicit object LoggableByte extends LoggableByte

  /**
   * Enables instances of `Short` to be logged by converting them to a string representation.
   *
   * This trait extends the `Loggable` type class for the `Short` type, providing an implementation
   * of the `toLog` method that uses the `toString` method to generate a string suitable for logging.
   */
  trait LoggableShort extends Loggable[Short] {
    def toLog(t: Short): String = t.toString
  }

  /**
   * Provides an implicit implementation of the `Loggable` type class for the `Short` type.
   *
   * This object allows instances of `Short` to be logged by converting them into their string
   * representation via the `toString` method. It extends the `LoggableShort` trait, which
   * defines the specific behavior for logging `Short` values.
   */
  implicit object LoggableShort extends LoggableShort

  /**
   * A specific implementation of the `Loggable` type class for `Int`.
   *
   * The `LoggableInt` trait provides a concrete implementation of the `toLog` method,
   * which converts an `Int` value to its string representation for logging purposes.
   */
  trait LoggableInt extends Loggable[Int] {
    /**
     * Converts the given integer to its string representation.
     *
     * @param t the integer to be converted
     * @return the string representation of the given integer
     */
    def toLog(t: Int): String = t.toString
  }

  /**
   * Implicit object that provides a `Loggable[Int]` implementation.
   * This enables logging functionality for `Int` by converting it to a string.
   * Used as an implicit instance of the `Loggable` type class for `Int`.
   */
  implicit object LoggableInt extends LoggableInt

  /**
   * A trait that extends the `Loggable` type class to provide a default implementation
   * for logging `Long` values. This allows long values to be rendered as Strings
   * for the purpose of logging in a laconic and compact representation.
   */
  trait LoggableLong extends Loggable[Long] {
    /**
     * Converts a `Long` value to its string representation for logging purposes.
     *
     * @param t the `Long` value to be converted to a string
     * @return the string representation of the input `Long` value
     */
    def toLog(t: Long): String = t.toString
  }

  /**
   * Implicit object providing a Loggable instance for the Long type.
   * This instance converts Long values to their string representation
   * by delegating to the toString method.
   *
   * It allows Long values to be logged in a consistent format wherever
   * a Loggable[Long] is implicitly required.
   */
  implicit object LoggableLong extends LoggableLong

  /**
   * A trait representing a `Loggable` instance for values of type `BigInt`.
   * It provides a default implementation for converting a `BigInt` to a compact loggable `String`.
   *
   * This trait allows `BigInt` instances to be rendered as a concise `String`
   * representation for logging purposes.
   */
  trait LoggableBigInt extends Loggable[BigInt] {
    /**
     * Converts a `BigInt` value into its string representation for logging purposes.
     *
     * @param t the `BigInt` value to be converted to a loggable string
     * @return the string representation of the input `BigInt`
     */
    def toLog(t: BigInt): String = t.toString
  }

  /**
   * Implicit object providing a Loggable implementation for the BigInt type.
   * Converts a BigInt instance to its string representation for logging purposes.
   */
  implicit object LoggableBigInt extends LoggableBigInt

  /**
   * A specialized implementation of the `Loggable` type class for the `String` type.
   *
   * This trait provides a default mechanism for logging `String` values by directly
   * returning the input string without any modification.
   */
  trait LoggableString extends Loggable[String] {
    /**
     * Converts the given string into a loggable format.
     *
     * @param t the string to be converted into loggable format
     * @return the string in a loggable format
     */
    def toLog(t: String): String = t
  }

  /**
   * Provides an implicit implementation of the Loggable trait for Strings.
   * Converts a String to its loggable representation by simply returning the string itself.
   */
  implicit object LoggableString extends LoggableString

  /**
   * A trait that provides a logging implementation for the `Double` type.
   *
   * This trait extends the `Loggable` type class for `Double` and defines a default
   * behavior to convert a `Double` value into its String representation for logging purposes.
   */
  trait LoggableDouble extends Loggable[Double] {
    /**
     * Converts a Double value to its String representation for logging.
     *
     * @param t the Double value to be converted to a String
     * @return the String representation of the provided Double value
     */
    def toLog(t: Double): String = t.toString
  }

  /**
   * Implicit object providing loggable functionality for Double values.
   *
   * Converts a Double into its string representation for logging purposes.
   */
  implicit object LoggableDouble extends LoggableDouble

  /**
   * A trait providing a specific implementation of the `Loggable` type class for the type `BigDecimal`.
   *
   * Extends the `Loggable` trait, enabling instances of `BigDecimal` to be rendered as compact Strings
   * for the purpose of logging. This implementation uses the `toString` method of `BigDecimal` to produce
   * the logged representation.
   */
  trait LoggableBigDecimal extends Loggable[BigDecimal] {
    /**
     * Converts a BigDecimal instance into its String representation for logging purposes.
     *
     * @param t the BigDecimal instance to be converted to a loggable String.
     * @return a String representation of the given BigDecimal, using its toString method.
     */
    def toLog(t: BigDecimal): String = t.toString
  }

  /**
   * Implicit object providing a `Loggable` implementation for `BigDecimal`.
   * This allows a `BigDecimal` to be logged by converting it to its string representation.
   */
  implicit object LoggableBigDecimal extends LoggableBigDecimal

  /**
   * An abstract class that provides logging capability for `Option` values by extending
   * the `Loggable` type class for `Option[T]`.
   *
   * @tparam T the underlying type contained within the `Option`.
   * @param evidence an implicit parameter ensuring a `Loggable` instance exists for type `T`.
   *
   *                 This class relies on an implicit `Loggable[Option[T]]` implementation provided by
   *                 `Loggables` to format `Option` values for logging purposes. The default representation
   *                 will use the format `Some(<value>)` for non-empty options and `None` for empty ones.
   */
  abstract class LoggableOption[T](implicit evidence: Loggable[T]) extends Loggable[Option[T]] {
    implicit private val z: Loggable[Option[T]] = new Loggables {}.optionLoggable

    /**
     * Converts the given `Option` value to its loggable string representation.
     *
     * @param t the `Option` value to be logged.
     * @return a string representation of the `Option` value, using the format `Some(<value>)` for non-empty values
     *         and `None` for empty ones, without newlines.
     */
    def toLog(t: Option[T]): String = z.toLog(t)
  }

  /**
   * An implicit object that defines a Loggable instance for `Option[Int]`.
   *
   * This allows values of type `Option[Int]` to be logged by providing
   * a specific implementation of the `toLog` method through the extension
   * of the `LoggableOption` class using `Int` as the type parameter.
   */
  implicit object LoggableOptionInt extends LoggableOption[Int]

  /**
   * Implicit object providing a `Loggable` instance for `Option[String]` types.
   * Enables logging functionality for optional string values, leveraging the existing `Loggable` context.
   * Automatically derives logging behavior by combining the `Loggable` instance for `String`
   * with the logic defined in `LoggableOption`.
   */
  implicit object LoggableOptionString extends LoggableOption[String]

  /**
   * A trait that extends the `Loggable` type class to provide logging functionality
   * for types that implement the `Temporal` interface.
   *
   * This trait specializes the generic `Loggable` interface for `Temporal`, a date-time abstraction
   * in the Java time API. It enables objects of type `Temporal` to be represented as strings
   * suitable for logging purposes.
   */
  trait LoggableTemporal extends Loggable[Temporal] {
    /**
     * Converts a given `Temporal` instance to its string representation suitable for logging.
     *
     * @param t the `Temporal` instance to be converted
     * @return a string representation of the given `Temporal` instance
     */
    def toLog(t: Temporal): String = t.toString
  }

  /**
   * Provides an implicit object that extends the `LoggableTemporal` trait.
   *
   * The LoggableTemporal object specializes the `Loggable` type class for `Temporal`
   * instances, enabling logging functionality specifically for date-time abstractions 
   * within the Java time API.
   *
   * It offers a mechanism for converting `Temporal` instances into their string 
   * representation, which can be used in logging frameworks or other contexts 
   * requiring a string-based representation of date-time values.
   */
  implicit object LoggableTemporal extends LoggableTemporal

}
