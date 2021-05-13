[![CircleCI](https://circleci.com/gh/rchillyard/Flog.svg?style=svg)](https://circleci.com/gh/rchillyard/Flog)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/aa3d2f49a67f4ce58b702f4403092290)](https://www.codacy.com/gh/rchillyard/Flog/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=rchillyard/Flog&amp;utm_campaign=Badge_Grade)
![GitHub Top Languages](https://img.shields.io/github/languages/top/rchillyard/Flog)
![GitHub](https://img.shields.io/github/license/rchillyard/Flog)
![GitHub last commit](https://img.shields.io/github/last-commit/rchillyard/Flog)
![GitHub issues](https://img.shields.io/github/issues-raw/rchillyard/Flog)
![GitHub issues by-label](https://img.shields.io/github/issues/rchillyard/Flog/bug)

# Flog
This is a set of utilities for functional logging.
You can copy the jar files, etc. from the _releases_ directory.

## Introduction and Usage
_Flog_ is a functional logger:
That's to say that _Flog_ is expression-oriented rather than statement-oriented.

In a statement-oriented language such as Java, it is reasonably convenient to add an extra logging line to a method or
block.
However, when writing functional programs, it's very inconvenient to be forced to break up the flow and perhaps declare
a value, then log the value, then continue to use the value.
Therefore, in this functional logger, we write loggable expressions which yield a value and, as a side effect--
which the rest of the program doesn't "see" -- we do the logging (footnote 1).

We define an instance of _Flog_, import its properties, and then use the !! method (actually a method
on an implicit inner class of _Flog_ called _Floggable_ which has a _String_ parameter in its constructor).

The basic usage pattern is thus:

    val flog = Flog()
    import flog._
    val x: X = msg !! expr

where _msg_ evaluates to a String and _expr_ evaluates to a value of type _X_ which will be assigned to _x_ (footnote 2)
while, as a side effect, the value of _expr_ is logged.
In other words, if you take away the "msg !!" the program will work exactly the same, but without the side effect of
logging.

Because we want to control the way a log message looks, we define the trait _Loggable[X]_ which is a type constructor.
In particular, we need reasonably brief but informative strings.
Specific loggable behaviors are defined, therefore, in implicit objects.
Those that are provided by _Flog_ are defined in the _Loggable_ companion object (see below).
For example, when logging an iterable, we show the start and end of the list, and simply count the intervening number.
A further advantage of this mechanism is that we can define the various methods involving _Loggable_ to be
call-by-name (i.e. non-strict).
This avoids constructing the string when logging is turned off.

In addition to the !! method,
there is also !| for logging a generic type that isn't necessarily _Loggable_.
In this case, we simply invoke _toString_ on the object to get a rendition for logging.
There's also a |! method which ignores the message and does no logging at all.
This is useful if you want to temporarily suspend a particular logging construct without removing the instrumentation.

The following signatures are defined for _Floggable_ (the implicit class):

    def !![X: Loggable](x: => X): X
    def !![K: Loggable, V: Loggable](kVm: => Map[K,V]): Map[K,V]
    def !![X: Loggable](x: => Iterable[X]): Iterable[X]
    def !![X: Loggable](x: => Option[X]): Option[X]
    def !|[X](x: => X): X // logs using x.toString
    def |![X](x: => X): X // does no logging
    def !!![X: Loggable](xy: Try[X]): Try[X]

The signature which takes an _Iterable[X]_ does require some further discussion.
If there are sufficient elements, the first three elements and the last element are shown in the log message.
Only the number of non-logged elements is shown between them.
This method is also invoked by the !!(Map) method, seeing an _Iterable[(String, String)]_.

In the case of non-strict collections, no unnecessary evaluation is performed.
Views are left as is and *LazyList*s are shown as lists only if they have definite size.

The last-named (!!!) method does not return the input exactly as is (as all the other methods do).
If _xy_ is a _Failure(e)_ then it logs the exception and returns _Failure(LoggedException(e))_.
This allows for the code to avoid logging the exception twice.

For all these !! logging mechanism to work, there must be (implicit) evidence of _Loggable[X]_ available.
The following standard _Loggables_ are provided:

    implicit object LoggableBoolean extends Loggable[Boolean]
    implicit object LoggableByte extends Loggable[Byte]
    implicit object LoggableShort extends Loggable[Short]
    implicit object LoggableInt extends Loggable[Int]
    implicit object LoggableLong extends Loggable[Long]
    implicit object LoggableBigInt extends Loggable[BigInt]
    implicit object LoggableString extends Loggable[String]
    implicit object LoggableDouble extends Loggable[Double]
    implicit object LoggableBigDecimal extends Loggable[BigDecimal]
    implicit object LoggableUnit extends Loggable[Unit]

Additionally, for those container types which are not explicitly handled by the !! method signatures,
there is support, in _Loggables_, for various specific types of containers to be logged
where, in each case, the parametric types _T_, _L_, _R_, _K_, or _V_ must provide implicit evidence of type _Loggable[T]_, etc.:

    def optionLoggable[T: Loggable]: Loggable[Option[T]]
    def iterableLoggable[T: Loggable]: Loggable[Iterable[T]]
    def mapLoggable[K, T: Loggable]: Loggable[Map[K, T]] 
    def tryLoggable[T: Loggable]: Loggable[Try[T]]
    def futureLoggable[T: Loggable](implicit logFunc: LogFunction, ec: ExecutionContext): Loggable[Future[T]] // this produces two log messages: on promise and on completion
    def eitherLoggable[L: Loggable, R: Loggable]: Loggable[Either[L, R]]
    def kVLoggable[K: Loggable, V: Loggable]: Loggable[(K, V)]

There is also a method which can be used for any underlying type that for which there
is no explicit loggable method:

    def anyLoggable[T]: Loggable[T]

Additionally, _Loggables_ defines a set of methods for creating _Loggable[P]_ where _P_ is a _Product_,
that's to say a case class, or a tuple, with a particular number of members (fields).
Each member type must itself be _Loggable_
("standard" types such as Int and String--see above for definition--will implicitly find the appropriate instance).
If a member is of a non-standard type, you will need to define an implicit val with the appropriate method
from _Loggables_ (see above).
For these methods, all you have to do is include a reference to the _apply_ method of the case class
(you can skip the ".apply" if you haven't defined an explicit companion object).
If you are familiar with, for instance, reading/writing Json, you should be comfortable with this idea.

Each of these "loggableN" methods has a signature thus (using _loggable3_ as an exemplar):

    def loggable3[P0: Loggable, P1: Loggable, P2: Loggable, T <: Product : ClassTag]
        (construct: (P0, P1, P2) => T, fields: Seq[String] = Nil): Loggable[T]

There are currently 9 such methods defined (_loggable1_ thru _loggable9_).
Here is the specification used to test this particular method:

    case class Threesy(x: Int, y: Boolean, z: Double)
    val target = loggable3(Threesy)
    target.toLog(Threesy(42, y = true, 3.1415927)) shouldBe "Threesy(x:42,y:true,z:3.1415927)"

In some situations, the reflection code is unable to get the field names in order (for example when there are public lazy vals).
In such a case, add the second parameter (after the function) to explicitly give the field names in order.
Normally, of course, you can leave this parameter unset.

Please see _worksheets/FlogExamples.sc_ for examples of usage.
Additionally, see any of the spec files, especially _FlogSpec_ for more definition on how to use the package.

If you wish to make a class loggable which is not a case class (or other _Product_),
then you can do it something like the following (basically you must define the _toLog_ method):

    class Complex(val real: Double, val imag: Double)
    object Complex {
      trait LoggableComplex extends Loggable[Complex] {
        def toLog(t: Complex): String = s"${t.real} + i${t.imag}"
      }
      implicit object LoggableComplex extends LoggableComplex
    }

### Variations
_Flog_ is a case class which has two members: _loggingFunction_ and _errorFunction_.
Normally, you will use the defaults for these.
However, if you do want to provide your own, then you need to understand
their type, another case class:

    case class LogFunction(f: String => Any, enabled: Boolean = true)

It is also possible to change the behavior of the _Flog_ instance by invoking one of the methods:

    def disabled: Flog
    def withLogFunction(logFunc: LogFunction): Flog

The default logger function uses _org.slf4j.LoggerFactory.getLogger_ to provide a logger.

You can create a _Flog_ instance based on logging for a particular class by starting with (where _MyClass_ is the class):

    val flog = Flog.forClass[MyClass]

or

    val flog = Flog.forClass(classOf[MyClass])

## Dependencies
For the default logging function, we include the following dependencies:

    "org.slf4j" % "slf4j-api" % "1.7.30",
    "ch.qos.logback" % "logback-classic" % "1.2.3" % "runtime"

If you choose to use a different logger function, you may need to change these dependencies.

## Footnotes
* (1) At present, this mechanism is not truly referentially transparent.
  In the future, we may provide an actor mechanism to allow for pure functional logging which is RT.
* (2) "assigned to x:" I don't mean to suggest assignment in the classic sense any more than I mean that "variables"
  are mutable.
  The construct "val x = expr" means that _x_ and (the evaluated) _expr_ mean the same in the remainder
  of the current scope.
  
# Version
1.0.7 Issue #14: Implemented level-based logging;

1.0.6 Issue #12: Minor changes to iterableLoggable;

1.0.5 Issue #10: Some changes to implementation of Iterable, including not evaluating non-strict collections.

1.0.4 Issue #7: Provides a more functional way of setting an explicit logger or disabling logging.

1.0.3 General improvements: more consistent functionality, issues with underlying logger hopefully resolved.

1.0.2 Added support for Future, cleaned up non-Flog modules, changed
artifact name to "flog."

1.0.1 This project was cloned from DecisionTree.
