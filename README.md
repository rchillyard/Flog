[![CircleCI](https://circleci.com/gh/rchillyard/Flog.svg?style=svg)](https://circleci.com/gh/rchillyard/Flog)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/aa3d2f49a67f4ce58b702f4403092290)](https://www.codacy.com/gh/rchillyard/Flog/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=rchillyard/Flog&amp;utm_campaign=Badge_Grade)

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
    def !![X: Loggable](x: => Iterable[X]): Iterable[X]
    def !![X: Loggable](x: => Option[X]): Option[X]
    def !|[X](x: => X): X // logs using x.toString
    def |![X](x: => X): X // does no logging

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
where, in each case, the parametric types _T_, _L_, or _R_ provide implicit evidence of type _Loggable[T]_, etc.:

    Seq[T]
    List[T]
    Vector[T]
    Map[K, T]
    Option[T]
    Try[T]
    Future[T] (this produces two log messages: on promise and completion)
    Either[L, R] (where each of L and R are Loggable)
    and _Product_ type up to _Product7_ (case classes and tuples) where each member type is _Loggable_.

Please see worksheets/FlogExamples.sc for examples of usage.
Additionally, see any of the spec files, especially _FlogSpec_ for more definition on how to use the package.

### Variations
It is possible to change the behavior of the _Flog_ instance by invoking one of the methods:

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
1.0.4 Provides a more functional way of setting an explicit logger or disabling logging.

1.0.3 General improvements: more consistent functionality, issues with underlying logger hopefully resolved.

1.0.2 Added support for Future, cleaned up non-Flog modules, changed
artifact name to "flog."

1.0.1 This project was cloned from DecisionTree.
