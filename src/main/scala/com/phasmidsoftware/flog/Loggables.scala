/*
 * Copyright (c) 2019. Phasmid Software
 */

package com.phasmidsoftware.flog

import com.phasmidsoftware.flog.Loggables.fieldNames
import scala.collection.SeqMap
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

/**
 * Trait to define methods for rendering instances of case classes (with their various parameters),
 * containers (Seq and Option), etc..
 */
trait Loggables {

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
   * Method to return a Loggable[ Seq[T] ].
   *
   * @tparam T the underlying type of the parameter of the input to the toLog method.
   * @return a Loggable[ List[T] ]
   */
  def seqLoggable[T: Loggable]: Loggable[Seq[T]] = (ts: Seq[T]) => {
    val tl = implicitly[Loggable[T]]
    ts match {
      case Nil => "[]"
      case h :: Nil => s"[${tl.toLog(h)}]"
      case h :: k :: tail =>
        val remainder = tail.size - 1
        val meat = if (remainder > 0) s"... ($remainder elements), ... " else ""
        s"[${tl.toLog(h)}, ${tl.toLog(k)}, $meat${tl.toLog(tail.last)}]"
      case h :: tail =>
        // XXX merge these cases
        val remainder = tail.size - 1
        val meat = if (remainder > 0) s"... ($remainder elements), ... " else ""
        s"[${tl.toLog(h)}, $meat${tl.toLog(tail.last)}]"
    }
  }

  /**
   * Method to return a Loggable[ List[T] ].
   *
   * @tparam T the underlying type of the parameter of the input to the toLog method.
   * @return a Loggable[ List[T] ]
   */
  def listLoggable[T: Loggable]: Loggable[List[T]] = {
    ts => seqLoggable[T].toLog(ts)
  }

  /**
   * Method to return a Loggable[ Vector[T] ].
   *
   * @tparam T the underlying type of the parameter of the input to the toLog method.
   * @return a Loggable[ Vector[T] ]
   */
  def vectorLoggable[T: Loggable]: Loggable[Vector[T]] = {
    case v: Vector[T] => seqLoggable[T].toLog(v.toList)
  }

  /**
   * Method to return a Loggable[ Map[K, T] ].
   *
   * NOTE: unless the Map (passed as the bound variable) is a SeqMap, the order of the key-value pairs is undefined.
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
   * Method to return a Loggable[ Either[T,U] ].
   *
   * @tparam T the underlying type of the first parameter of the input to the render method.
   * @return a Loggable[ Either[T,U] ].
   */
  def eitherLoggable[T: Loggable, U: Loggable]: Loggable[Either[T, U]] = {
    case Left(t: T@unchecked) => s"Left(${implicitly[Loggable[T]].toLog(t)})"
    case Right(u: Vector[T]@unchecked) => val lv = new Loggables {}.vectorLoggable[T]; s"Right(${lv.toLog(u)})"
    case Right(u: U@unchecked) => s"Right(${implicitly[Loggable[U]].toLog(u)})"
    case x => s"<problem with logging Either: $x"
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
   * Method to return a Loggable[ Future[T] ].
   *
   * @tparam T the underlying type of the first parameter of the input to the render method.
   * @return a Loggable[ Future[T] ].
   */
  def futureLoggable[T: Loggable](implicit logFunc: LogFunction, ec: ExecutionContext): Loggable[Future[T]] = (tf: Future[T]) => {
    val uuid = java.util.UUID.randomUUID
    implicit val tl: Loggable[Try[T]] = tryLoggable
    tf.onComplete(ty => logFunc(s"Future completed ($uuid): ${tl.toLog(ty)}"))
    s"Future: promise ($uuid) created... "
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

  private def valueToLog[P: Loggable, T <: Product](t: T, i: Int): String = implicitly[Loggable[P]].toLog(t.productElement(i).asInstanceOf[P])
}

object Loggables {

  private def fieldNames[T: ClassTag](fields: Seq[String], method: String): Array[String] = fields match {
    case Nil => extractFieldNames(implicitly[ClassTag[T]], method)
    case ps => ps.toArray
  }

  private def extractFieldNames(classTag: ClassTag[_], method: String): Array[String] = {
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
