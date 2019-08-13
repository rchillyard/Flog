/*
 * Copyright (c) 2019. Phasmid Software
 */

package com.phasmidsoftware.util

/**
 * Type class to generate showable types.
 *
 * @tparam T the type to be shown.
 */
trait Show[T] {
  /**
   * Method to yield a neat String representation of a T.
   *
   * @param t the instance of T to be shown.
   * @return a String, not necessarily a single-line String (as in the case of Loggable),
   *         but not a String for debugging purposes either.
   */
  def show(t: T): String
}
