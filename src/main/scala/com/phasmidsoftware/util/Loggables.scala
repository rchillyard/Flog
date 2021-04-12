/*
 * Copyright (c) 2019. Phasmid Software
 */

package com.phasmidsoftware.util

import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

/**
 * Trait to define methods for rendering instances of case classes (with their various parameters),
 * containers (Seq and Option), etc..
 */
trait Loggables {

  /**
   * Method to return a Loggable[ Seq[T] ].
   *
   * @tparam T the underlying type of the first parameter of the input to the render method.
   * @return a Loggable[ Seq[T] ]
   */
  def listLoggable[T: Loggable]: Loggable[List[T]] = (ts: List[T]) => {
    val tl = implicitly[Loggable[T]]
    ts match {
      case Nil => "[]"
      case h :: Nil => s"[${tl.toLog(h)}]"
      case h :: tail =>
        val remainder = tail.size - 1
        val meat = if (remainder > 0) s"... ($remainder elements), ... " else ""
        s"[${tl.toLog(h)}, $meat${tl.toLog(tail.last)}]"
    }
  }

  /**
   * Method to return a Loggable[ Vector[T] ].
   *
   * @tparam T the underlying type of the first parameter of the input to the render method.
   * @return a Loggable[ Vector[T] ]
   */
  def vectorLoggable[T: Loggable]: Loggable[Vector[T]] = {
    case v: Vector[T] => listLoggable[T].toLog(v.toList)
  }

  /**
   * Method to return a Loggable[ Map[K, T] ].
   *
   * @tparam K the type of the keys.
   * @tparam T the underlying type of the values.
   * @return a Loggable[ Map[K, T] ]
   */
  def mapLoggable[K, T: Loggable](bookends: String = "{}"): Loggable[Map[K, T]] = (tKm: Map[K, T]) => {
    def z(k: K, t: T): String = k.toString + ":" + implicitly[Loggable[T]].toLog(t)

    tKm.map((z _).tupled).mkString(bookends.substring(0, 1), ",", bookends.substring(1, 2))
  }

  /**
   * Method to return a Loggable[ Option[T] ].
   *
   * @tparam T the underlying type of the first parameter of the input to the render method.
   * @return a Loggable[ Option[T] ].
   */
  def optionLoggable[T: Loggable]: Loggable[Option[T]] = {
    case Some(t: T@unchecked) => s"Some(${implicitly[Loggable[T]].toLog(t)})"
    case _ => "None"
  }

  /**
   * Method to return a Loggable[ Either[T,U] ].
   *
   * @tparam T the underlying type of the first parameter of the input to the render method.
   * @return a Loggable[ Either[T,U] ].
   */
  def eitherLoggable[T: Loggable, U: Loggable]: Loggable[Either[T, U]] = {
    case Left(_t: T@unchecked) => s"Left(${implicitly[Loggable[T]].toLog(_t)})"
    case Right(u: Vector[T]@unchecked) => val lv = new Loggables {}.vectorLoggable[T]; s"Right(${lv.toLog(u)})"
    case Right(u: U@unchecked) => s"Right(${implicitly[Loggable[U]].toLog(u)})"
    case x => s"<problem with logging Either: $x"
  }

  /**
   * Method to return a Loggable[ Try[T] ].
   *
   * @tparam T the underlying type of the first parameter of the input to the render method.
   * @return a Loggable[ Option[T] ].
   */
  def tryLoggable[T: Loggable]: Loggable[Try[T]] = {
    case Success(_t) => s"Success(${implicitly[Loggable[T]].toLog(_t)})"
    case Failure(x) => s"Failure(${x.getLocalizedMessage})"
  }

  /**
   * Method to return a Loggable[T] where T is a 1-ary Product and which is based on a function to convert a P into a T.
   *
   * NOTE: be careful using this particular method it only applies where T is a 1-tuple (e.g. a case class with one field -- not common).
   * It probably shouldn't ever be used in practice. It can cause strange initialization errors!
   * This note may be irrelevant now that we have overridden convertString to fix issue #1.
   *
   * @param construct a function P => T, usually the apply method of a case class.
   *                  The sole purpose of this function is for type inference--it is never actually invoked.
   * @param fields    an explicit list of one field name.
   * @tparam P0 the type of the (single) field of the Product type T.
   * @tparam T  the underlying type of the first parameter of the input to the render method.
   * @return a Loggable[T].
   */
  def toLog1[P0: Loggable, T <: Product : ClassTag](construct: P0 => T, fields: Seq[String] = Nil): Loggable[T] = (t: T) => {
    val Array(p0) = fields match {
      case Nil => Reflection.extractFieldNames(implicitly[ClassTag[T]], "toLog1")
      case ps => ps.toArray
    }
    t.productPrefix + mapLoggable[String, String]("()").toLog(Map(p0 -> valueToLog[P0, T](t, 0)
    )
    )
  }

  /**
   * Method to return a Loggable[T] where T is a 2-ary Product and which is based on a function to convert a (P1,P2) into a T.
   *
   * @param construct a function (P1,P2) => T, usually the apply method of a case class.
   *                  The sole purpose of this function is for type inference--it is never actually invoked.
   * @param fields    an explicit list of 2 field names.
   * @tparam P0 the type of the first field of the Product type T.
   * @tparam P1 the type of the second field of the Product type T.
   * @tparam T  the underlying type of the first parameter of the input to the render method.
   * @return a Loggable[T].
   */
  def toLog2[P0: Loggable, P1: Loggable, T <: Product : ClassTag](construct: (P0, P1) => T, fields: Seq[String] = Nil): Loggable[T] = (t: T) => {
    val Array(p0, p1) = fields match {
      case Nil => Reflection.extractFieldNames(implicitly[ClassTag[T]], "toLog2")
      case ps => ps.toArray
    }
    t.productPrefix + mapLoggable[String, String]("()").toLog(Map(
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
   * @param fields    an explicit list of 3 field names.
   * @tparam P0 the type of the first field of the Product type T.
   * @tparam P1 the type of the second field of the Product type T.
   * @tparam P2 the type of the third field of the Product type T.
   * @tparam T  the underlying type of the first parameter of the input to the render method.
   * @return a Loggable[T].
   */
  def toLog3[P0: Loggable, P1: Loggable, P2: Loggable, T <: Product : ClassTag]
  (construct: (P0, P1, P2) => T, fields: Seq[String] = Nil): Loggable[T] = (t: T) => {
    val Array(p0, p1, p2) = fields match {
      case Nil => Reflection.extractFieldNames(implicitly[ClassTag[T]], "toLog3")
      case ps => ps.toArray
    }
    t.productPrefix + mapLoggable[String, String]("()").toLog(Map(
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
   * @param fields    an explicit list of 4 field names.
   * @tparam P0 the type of the first field of the Product type T.
   * @tparam P1 the type of the second field of the Product type T.
   * @tparam P2 the type of the third field of the Product type T.
   * @tparam P3 the type of the fourth field of the Product type T.
   * @tparam T  the underlying type of the first parameter of the input to the render method.
   * @return a Loggable[T].
   */
  def toLog4[P0: Loggable, P1: Loggable, P2: Loggable, P3: Loggable, T <: Product : ClassTag]
  (construct: (P0, P1, P2, P3) => T, fields: Seq[String] = Nil): Loggable[T] = (t: T) => {
    val Array(p0, p1, p2, p3) = fields match {
      case Nil => Reflection.extractFieldNames(implicitly[ClassTag[T]], "toLog4")
      case ps => ps.toArray
    }
    t.productPrefix + mapLoggable[String, String]("()").toLog(Map(
      p0 -> valueToLog[P0, T](t, 0),
      p1 -> valueToLog[P1, T](t, 1),
      p2 -> valueToLog[P2, T](t, 2),
      p3 -> valueToLog[P3, T](t, 3)
    )
    )
  }

  def valueToLog[P: Loggable, T <: Product](t: T, i: Int): String = implicitly[Loggable[P]].toLog(t.productElement(i).asInstanceOf[P])

}

object Reflection {

  /**
   * This method is borrowed directly from Spray JsonReader.
   *
   * @param classTag rhw class tag.
   * @return an Array of String.
   */
  def extractFieldNames(classTag: ClassTag[_], method: String): Array[String] = {
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
