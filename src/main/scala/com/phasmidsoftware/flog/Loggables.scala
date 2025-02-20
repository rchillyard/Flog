/*
 * Copyright (c) 2021. Phasmid Software
 */

package com.phasmidsoftware.flog

import com.phasmidsoftware.flog.Loggables.fieldNames

import scala.collection.immutable.LazyList.#::
import scala.collection.{SeqMap, View}
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

/**
 * Trait to define methods for rendering instances of case classes (with their various parameters),
 * containers (Seq and Option), etc..
 */
trait Loggables {

  /**
   * Backstop method to yield a String from a T.
   * NOTE: use this when you need an implicit Loggable to log a container which includes an unsupported type.
   *
   * @tparam T the type of t.
   * @return a Loggable[T] which uses toString.
   */
  def anyLoggable[T]: Loggable[T] = (t: T) => t.toString

  /**
   * Method to return a Loggable[ Option[T] ].
   *
   * @tparam T the underlying type of the first parameter of the input to the render method.
   * @return a Loggable[ Option[T] ].
   */
  def optionLoggable[T: Loggable]: Loggable[Option[T]] = {
    case Some(t) => s"Some(${implicitly[Loggable[T]].toLog(t)})"
    case _ => "None"
  }

  /**
   * Method to create a Loggable of an Iterable[T].
   * The elements of the result are then logged utilizing the !! method.
   *
   * @param bookends (optional) an String of length two specifying the first and last characters of
   *                 the resulting String for a given Iterable. Defaults to "[]".
   * @param atLeast the minimum number of elements to show in the log message before the ellipsis section starts.
   * @tparam T the underlying type of the elements to be logged. Must provide evidence of Loggable[T].
   * @return Loggable[ Iterable[T] ].
   */
  def iterableLoggable[T: Loggable](bookends: String = "[]", atLeast: Int = 3): Loggable[Iterable[T]] = {
    case Nil => "<empty>"
    case Loggables.lazyNil => "<empty lazy list>"
    case ll@_ #:: _ =>
      if (ll.knownSize >= 0) iterableLoggable[T](bookends).toLog(ll.toList) else "<LazyList>"
    case _: View[T] =>
      "<view>"
    case ts =>
      val tl = implicitly[Loggable[T]]
      val ws = ts map tl.toLog
      val init = ws.init
      val n = init.size
      val (prefix, z) = if (n > atLeast) (init take atLeast, n - atLeast) else (init, 0)
      val remainder =
        if (z > 0)
          s"... ($z element" + (
              (if (z > 1) "s" else "")
                  + "), ... ")
        else ""
      val prefixString = if (prefix.nonEmpty) prefix.mkString("", ", ", ", ") else ""
      require(bookends.length == 2, "Bookends must have exactly two characters")
      bookends.substring(0, 1) + prefixString + remainder + ws.last + bookends.substring(1, 2)
  }

  /**
   * Method to return a Loggable[ Map[K, T] ].
   *
   * NOTE: unless the Map (passed as the bound variable) is a SeqMap, the order of the key-value pairs is undefined.
   *
   * @param bookends (optional) an String of length two specifying the first and last characters of
   *                 the resulting String for a given Map. Defaults to "{}".
   * @tparam K the type of the keys.
   * @tparam T the underlying type of the values.
   * @return a Loggable[ Map[K, T] ]
   */
  def mapLoggable[K, T: Loggable](bookends: String = "{}"): Loggable[Map[K, T]] = (tKm: Map[K, T]) => {
    def z(k: K, t: T): String = k.toString + ":" + implicitly[Loggable[T]].toLog(t)

    require(bookends.length == 2, "Bookends must have exactly two characters")
    tKm.map(z.tupled).mkString(bookends.substring(0, 1), ",", bookends.substring(1, 2))
  }

  /**
   * Method to return a Loggable[ Either[T,U] ].
   *
   * @tparam L the "left" type of any Either which is to be logged.
   * @tparam R the "right" type of any Either which is to be logged.
   * @return a Loggable[ Either[T,U] ].
   */
  def eitherLoggable[L: Loggable, R: Loggable]: Loggable[Either[L, R]] = {
    case Left(l: L@unchecked) => s"Left(${implicitly[Loggable[L]].toLog(l)})"
    case Right(ls: Iterable[L]@unchecked) => val lv = new Loggables {}.iterableLoggable[L](); s"Right(${lv.toLog(ls)})"
    case Right(r: R@unchecked) => s"Right(${implicitly[Loggable[R]].toLog(r)})"
  }

  /**
   * Method to return a Loggable[ Try[T] ].
   *
   * @tparam T the underlying type of the first parameter of the input to the render method.
   * @return a Loggable[ Try[T] ].
   */
  def tryLoggable[T: Loggable]: Loggable[Try[T]] = {
    case Success(t) => s"Success(${implicitly[Loggable[T]].toLog(t)})"
    case Failure(x) => s"Failure(${x.getLocalizedMessage})"
  }

  /**
   * TESTME
   *
   * Method which maps an Iterable of X with a function to an Iterable of Try[X].
   * The elements of the result are then logged utilizing the !! method.
   * NOTE that the returned value will only include the successful elements.
   *
   * @param f a function X => Try[X].
   * @tparam X the underlying type of xs.
   * @tparam Y the underlying type of the intermediate type (must be Loggable).
   * @return an Iterable of Try[X] such that all the failures have been logged but not included in the result.
   */
  def triedIterableLoggable[X, Y: Loggable](f: X => Try[Y]): Loggable[Iterable[X]] = (xs: Iterable[X]) => {
    implicit val q: Loggable[Try[Y]] = tryLoggable
    val z: Iterable[Try[Y]] = (for (x <- xs) yield f(x)) filter (_.isSuccess)
    val yys: Loggable[Iterable[Try[Y]]] = iterableLoggable[Try[Y]]()
    yys.toLog(z)
  }

  /**
   * This method creates a Loggable instance which works for a tuple (a K-V pair).
   * It is an alternative to loggable2 but that method names the members whereas we don't want them named here.
   *
   * @tparam K the key type.
   * @tparam V the value type.
   * @return a String rendition.
   */
  def kVLoggable[K: Loggable, V: Loggable]: Loggable[(K, V)] = (t: (K, V)) => s"${implicitly[Loggable[K]].toLog(t._1)}->${implicitly[Loggable[V]].toLog(t._2)}"

  /**
   * Method to return a Loggable[T] where T is a 1-ary Product and which is based on a function to convert a P into a T.
   *
   * NOTE: be careful using this particular method it only applies where T is a 1-tuple (e.g. a case class with one field -- not common).
   * It probably shouldn't ever be used in practice. It can cause strange initialization errors!
   * This note may be irrelevant now that we have overridden convertString to fix issue #1.
   *
   * @param construct a function P => T, usually the apply method of a case class.
   *                  The sole purpose of this function is for type inference--it is never actually invoked.
   * @param fields    (optional parameter) an explicit list of one field name.
   * @tparam P0 the type of the (single) field of the Product type T.
   * @tparam T  the underlying type of the first parameter of the input to the render method.
   * @return a Loggable[T].
   */
  def loggable1[P0: Loggable, T <: Product : ClassTag](construct: P0 => T, fields: Seq[String] = Nil): Loggable[T] = (t: T) => {
    val Array(p0) = fieldNames(fields, "loggable1")
    t.productPrefix + mapLoggable[String, String]("()").toLog(SeqMap(p0 -> valueToLog[P0, T](t, 0)
    )
    )
  }

  /**
   * Method to return a Loggable[T] where T is a 2-ary Product and which is based on a function to convert a (P1,P2) into a T.
   *
   * NOTE: please see project TableParser for ideas on how to define toLogN+1 in terms of toLogN.
   *
   * @param construct a function (P1,P2) => T, usually the apply method of a case class.
   *                  The sole purpose of this function is for type inference--it is never actually invoked.
   * @param fields    (optional parameter) an explicit list of 2 field names.
   * @tparam P0 the type of the first field of the Product type T.
   * @tparam P1 the type of the second field of the Product type T.
   * @tparam T  the underlying type of the first parameter of the input to the render method.
   * @return a Loggable[T].
   */
  def loggable2[P0: Loggable, P1: Loggable, T <: Product : ClassTag](construct: (P0, P1) => T, fields: Seq[String] = Nil): Loggable[T] = (t: T) => {
    val Array(p0, p1) = fieldNames(fields, "loggable2")
    t.productPrefix + mapLoggable[String, String]("()").toLog(SeqMap(
      p0 -> valueToLog[P0, T](t, 0),
      p1 -> valueToLog[P1, T](t, 1)
    )
    )
  }

  /**
   * Method to return a Loggable[T] where T is a 3-ary Product and which is based on a function to convert a (P1,P2,P3) into a T.
   *
   * @param construct a function (P1,P2,P3) => T, usually the apply method of a case class.
   *                  The sole purpose of this function is for type inference--it is never actually invoked.
   * @param fields    (optional parameter) an explicit list of 3 field names.
   * @tparam P0 the type of the first field of the Product type T.
   * @tparam P1 the type of the second field of the Product type T.
   * @tparam P2 the type of the third field of the Product type T.
   * @tparam T  the underlying type of the first parameter of the input to the render method.
   * @return a Loggable[T].
   */
  def loggable3[P0: Loggable, P1: Loggable, P2: Loggable, T <: Product : ClassTag]
  (construct: (P0, P1, P2) => T, fields: Seq[String] = Nil): Loggable[T] = (t: T) => {
    val Array(p0, p1, p2) = fieldNames(fields, "loggable3")
    t.productPrefix + mapLoggable[String, String]("()").toLog(SeqMap(
      p0 -> valueToLog[P0, T](t, 0),
      p1 -> valueToLog[P1, T](t, 1),
      p2 -> valueToLog[P2, T](t, 2)
    )
    )
  }

  /**
   * Method to return a Loggable[T] where T is a 4-ary Product and which is based on a function to convert a (P0,P1,P2,P3) into a T.
   *
   * @param construct a function (P0,P1,P2,P3) => T, usually the apply method of a case class.
   *                  The sole purpose of this function is for type inference--it is never actually invoked.
   * @param fields    (optional parameter) an explicit list of 4 field names.
   * @tparam P0 the type of the first field of the Product type T.
   * @tparam P1 the type of the second field of the Product type T.
   * @tparam P2 the type of the third field of the Product type T.
   * @tparam P3 the type of the fourth field of the Product type T.
   * @tparam T  the underlying type of the first parameter of the input to the render method.
   * @return a Loggable[T].
   */
  def loggable4[P0: Loggable, P1: Loggable, P2: Loggable, P3: Loggable, T <: Product : ClassTag]
  (construct: (P0, P1, P2, P3) => T, fields: Seq[String] = Nil): Loggable[T] = (t: T) => {
    val Array(p0, p1, p2, p3) = fieldNames(fields, "loggable4")
    t.productPrefix + mapLoggable[String, String]("()").toLog(SeqMap(
      p0 -> valueToLog[P0, T](t, 0),
      p1 -> valueToLog[P1, T](t, 1),
      p2 -> valueToLog[P2, T](t, 2),
      p3 -> valueToLog[P3, T](t, 3)
    )
    )
  }

  /**
   * Method to return a Loggable[T] where T is a 5-ary Product and which is based on a function to
   * convert a (P0,P1,P2,P3,P4) into a T.
   *
   * @param construct a function (P0,P1,P2,P3,P4) => T, usually the apply method of a case class.
   *                  The sole purpose of this function is for type inference--it is never actually invoked.
   * @param fields    (optional parameter) an explicit list of 5 field names.
   * @tparam P0 the type of the first field of the Product type T.
   * @tparam P1 the type of the second field of the Product type T.
   * @tparam P2 the type of the third field of the Product type T.
   * @tparam P3 the type of the fourth field of the Product type T.
   * @tparam P4 the type of the fifth field of the Product type T.
   * @tparam T  the underlying type of the first parameter of the input to the render method.
   * @return a Loggable[T].
   */
  def loggable5[P0: Loggable, P1: Loggable, P2: Loggable, P3: Loggable, P4: Loggable, T <: Product : ClassTag]
  (construct: (P0, P1, P2, P3, P4) => T, fields: Seq[String] = Nil): Loggable[T] = (t: T) => {
    val Array(p0, p1, p2, p3, p4) = fieldNames(fields, "loggable5")
    t.productPrefix + mapLoggable[String, String]("()").toLog(SeqMap(
      p0 -> valueToLog[P0, T](t, 0),
      p1 -> valueToLog[P1, T](t, 1),
      p2 -> valueToLog[P2, T](t, 2),
      p3 -> valueToLog[P3, T](t, 3),
      p4 -> valueToLog[P4, T](t, 4)
    )
    )
  }

  /**
   * Method to return a Loggable[T] where T is a 6-ary Product and which is based on a function to
   * convert a (P0,P1,P2,P3,P4,P5) into a T.
   *
   * @param construct a function (P0,P1,P2,P3,P4,P5) => T, usually the apply method of a case class.
   *                  The sole purpose of this function is for type inference--it is never actually invoked.
   * @param fields    (optional parameter) an explicit list of 6 field names.
   * @tparam P0 the type of the first field of the Product type T.
   * @tparam P1 the type of the second field of the Product type T.
   * @tparam P2 the type of the third field of the Product type T.
   * @tparam P3 the type of the fourth field of the Product type T.
   * @tparam P4 the type of the fifth field of the Product type T.
   * @tparam P5 the type of the sixth field of the Product type T.
   * @tparam T  the underlying type of the first parameter of the input to the render method.
   * @return a Loggable[T].
   */
  def loggable6[P0: Loggable, P1: Loggable, P2: Loggable, P3: Loggable, P4: Loggable, P5: Loggable, T <: Product : ClassTag]
  (construct: (P0, P1, P2, P3, P4, P5) => T, fields: Seq[String] = Nil): Loggable[T] = (t: T) => {
    val Array(p0, p1, p2, p3, p4, p5) = fieldNames(fields, "loggable6")
    t.productPrefix + mapLoggable[String, String]("()").toLog(SeqMap(
      p0 -> valueToLog[P0, T](t, 0),
      p1 -> valueToLog[P1, T](t, 1),
      p2 -> valueToLog[P2, T](t, 2),
      p3 -> valueToLog[P3, T](t, 3),
      p4 -> valueToLog[P4, T](t, 4),
      p5 -> valueToLog[P5, T](t, 5)
    )
    )
  }

  /**
   * Method to return a Loggable[T] where T is a 7-ary Product and which is based on a function to
   * convert a (P0,P1,P2,P3,P4,P5,P6) into a T.
   *
   * @param construct a function (P0,P1,P2,P3,P4,P5,P6) => T, usually the apply method of a case class.
   *                  The sole purpose of this function is for type inference--it is never actually invoked.
   * @param fields    (optional parameter) an explicit list of 7 field names.
   * @tparam P0 the type of the first field of the Product type T.
   * @tparam P1 the type of the second field of the Product type T.
   * @tparam P2 the type of the third field of the Product type T.
   * @tparam P3 the type of the fourth field of the Product type T.
   * @tparam P4 the type of the fifth field of the Product type T.
   * @tparam P5 the type of the sixth field of the Product type T.
   * @tparam P6 the type of the seventh field of the Product type T.
   * @tparam T  the underlying type of the first parameter of the input to the render method.
   * @return a Loggable[T].
   */
  def loggable7[P0: Loggable, P1: Loggable, P2: Loggable, P3: Loggable, P4: Loggable, P5: Loggable, P6: Loggable, T <: Product : ClassTag]
  (construct: (P0, P1, P2, P3, P4, P5, P6) => T, fields: Seq[String] = Nil): Loggable[T] = (t: T) => {
    val Array(p0, p1, p2, p3, p4, p5, p6) = fieldNames(fields, "loggable7")
    t.productPrefix + mapLoggable[String, String]("()").toLog(SeqMap(
      p0 -> valueToLog[P0, T](t, 0),
      p1 -> valueToLog[P1, T](t, 1),
      p2 -> valueToLog[P2, T](t, 2),
      p3 -> valueToLog[P3, T](t, 3),
      p4 -> valueToLog[P4, T](t, 4),
      p5 -> valueToLog[P5, T](t, 5),
      p6 -> valueToLog[P6, T](t, 6)
    )
    )
  }

  /**
   * Method to return a Loggable[T] where T is a 8-ary Product and which is based on a function to
   * convert a (P0,P1,P2,P3,P4,P5,P6,P7) into a T.
   *
   * @param construct a function (P0,P1,P2,P3,P4,P5,P6,P7) => T, usually the apply method of a case class.
   *                  The sole purpose of this function is for type inference--it is never actually invoked.
   * @param fields    (optional parameter) an explicit list of 8 field names.
   * @tparam P0 the type of the first field of the Product type T.
   * @tparam P1 the type of the second field of the Product type T.
   * @tparam P2 the type of the third field of the Product type T.
   * @tparam P3 the type of the fourth field of the Product type T.
   * @tparam P4 the type of the fifth field of the Product type T.
   * @tparam P5 the type of the sixth field of the Product type T.
   * @tparam P6 the type of the seventh field of the Product type T.
   * @tparam P7 the type of the eighth field of the Product type T.
   * @tparam T  the underlying type of the first parameter of the input to the render method.
   * @return a Loggable[T].
   */
  def loggable8[P0: Loggable, P1: Loggable, P2: Loggable, P3: Loggable, P4: Loggable, P5: Loggable, P6: Loggable, P7: Loggable, T <: Product : ClassTag]
  (construct: (P0, P1, P2, P3, P4, P5, P6, P7) => T, fields: Seq[String] = Nil): Loggable[T] = (t: T) => {
    val Array(p0, p1, p2, p3, p4, p5, p6, p7) = fieldNames(fields, "loggable7")
    t.productPrefix + mapLoggable[String, String]("()").toLog(SeqMap(
      p0 -> valueToLog[P0, T](t, 0),
      p1 -> valueToLog[P1, T](t, 1),
      p2 -> valueToLog[P2, T](t, 2),
      p3 -> valueToLog[P3, T](t, 3),
      p4 -> valueToLog[P4, T](t, 4),
      p5 -> valueToLog[P5, T](t, 5),
      p6 -> valueToLog[P6, T](t, 6),
      p7 -> valueToLog[P7, T](t, 7)
    )
    )
  }

  /**
   * Method to return a Loggable[T] where T is a 9-ary Product and which is based on a function to
   * convert a (P0,P1,P2,P3,P4,P5,P6,P7,P8) into a T.
   *
   * @param construct a function (P0,P1,P2,P3,P4,P5,P6,P7,P8) => T, usually the apply method of a case class.
   *                  The sole purpose of this function is for type inference--it is never actually invoked.
   * @param fields    (optional parameter) an explicit list of 9 field names.
   * @tparam P0 the type of the first field of the Product type T.
   * @tparam P1 the type of the second field of the Product type T.
   * @tparam P2 the type of the third field of the Product type T.
   * @tparam P3 the type of the fourth field of the Product type T.
   * @tparam P4 the type of the fifth field of the Product type T.
   * @tparam P5 the type of the sixth field of the Product type T.
   * @tparam P6 the type of the seventh field of the Product type T.
   * @tparam P7 the type of the eighth field of the Product type T.
   * @tparam P8 the type of the ninth field of the Product type T.
   * @tparam T  the underlying type of the first parameter of the input to the render method.
   * @return a Loggable[T].
   */
  def loggable9[P0: Loggable, P1: Loggable, P2: Loggable, P3: Loggable, P4: Loggable, P5: Loggable, P6: Loggable, P7: Loggable, P8: Loggable, T <: Product : ClassTag]
  (construct: (P0, P1, P2, P3, P4, P5, P6, P7, P8) => T, fields: Seq[String] = Nil): Loggable[T] = (t: T) => {
    val Array(p0, p1, p2, p3, p4, p5, p6, p7, p8) = fieldNames(fields, "loggable7")
    t.productPrefix + mapLoggable[String, String]("()").toLog(SeqMap(
      p0 -> valueToLog[P0, T](t, 0),
      p1 -> valueToLog[P1, T](t, 1),
      p2 -> valueToLog[P2, T](t, 2),
      p3 -> valueToLog[P3, T](t, 3),
      p4 -> valueToLog[P4, T](t, 4),
      p5 -> valueToLog[P5, T](t, 5),
      p6 -> valueToLog[P6, T](t, 6),
      p7 -> valueToLog[P7, T](t, 7),
      p8 -> valueToLog[P8, T](t, 8)
    )
    )
  }

  private def valueToLog[P: Loggable, T <: Product](t: T, i: Int): String = implicitly[Loggable[P]].toLog(t.productElement(i).asInstanceOf[P])
}

object Loggables {

  private val lazyNil = LazyList.empty

  /**
   * Retrieves an array of field names from a given sequence of strings, or extracts them based on a specified method
   * if the input sequence is empty.
   *
   * @param fields a sequence of field names provided explicitly; if empty, the field names will be extracted
   *               automatically based on the type parameter and method.
   * @param method the name of the method used during automatic extraction for identifying case class fields.
   * @tparam T the type parameter representing the expected class type from which fields may be extracted.
   * @return an array of field names either derived from the input sequence or extracted based on the provided type and method.
   */
  private def fieldNames[T: ClassTag](fields: Seq[String], method: String): Array[String] = fields match {
    case Nil => extractFieldNames(implicitly[ClassTag[T]], method)
    case ps => ps.toArray
  }

  /**
   * Extracts the field names of a case class using reflection based on the class type provided in the `ClassTag`.
   * This method ensures that field names match the declaration order within the case class. It also compares the
   * declared fields with the associated Scala-generated methods to ensure consistency.
   *
   * @param classTag The `ClassTag` of the class whose field names are to be extracted.
   * @param method   The name of the calling method for error reporting purposes.
   * @return An array of field names extracted from the specified case class.
   */
  private def extractFieldNames(classTag: ClassTag[?], method: String): Array[String] = {
    import java.lang.reflect.Modifier
    import scala.util.control.NonFatal

    val clazz = classTag.runtimeClass
    try {
      // NOTE: copy methods have the form copy$default$N(), we need to sort them in order, but must account for the fact
      // ... that lexical sorting of ...8(), ...9(), ...10() is not correct, so we extract N and sort by N.toInt
      val copyDefaultMethods = clazz.getMethods.filter(_.getName.startsWith("copy$default$")).sortBy(
        _.getName.drop("copy$default$".length).takeWhile(_ != '(').toInt)
      val fields = clazz.getDeclaredFields.filterNot { f =>
        import Modifier._
        (f.getModifiers & (TRANSIENT | STATIC | 0x1000 /* SYNTHETIC*/)) > 0
      }
      if (copyDefaultMethods.length != fields.length)
        sys.error("Case class " + clazz.getName + " declares additional fields")
      if (fields.zip(copyDefaultMethods).exists { case (f, m) => f.getType != m.getReturnType })
        sys.error("Cannot determine field order of case class " + clazz.getName)
      fields.map(f => f.getName)
    } catch {
      case NonFatal(ex) => throw new RuntimeException("Cannot automatically determine case class field names and order " +
          s"for '${clazz.getName}', please provide an explicit list of fields in the second parameter of method $method", ex)
    }
  }
}
