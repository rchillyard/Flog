[![Codacy Badge](https://api.codacy.com/project/badge/Grade/2d89f95b27b246e3bd1c3c116ff24004)](https://www.codacy.com/app/scalaprof/Util?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=rchillyard/Util&amp;utm_campaign=Badge_Grade)
[![CircleCI](https://circleci.com/gh/rchillyard/Util.svg?style=svg)](https://circleci.com/gh/rchillyard/Util)

# Util Introduction
This is a set of utilities such as functional logging.
You can copy the jar files, etc. from the releases directory.

## Flog
This project contains various utilities.
Perhaps the most useful is the functional logger: _Flog_.
Please see _FlogSpec_ for ideas on how to use it.
The basic idea is this:

    import Flog._
    val x: X = msg !! expr

where _msg_ evaluates to a String and _expr_ evaluates to a value of type _X_ which will be assigned to _x_.
In other words, if you take away the "msg !!" the program will work exactly the same, but without the side effect of
logging.
The space separating _msg_ from "!!" is optional and leaving it out may make it easier to eliminate logging which
was added temporarily.

For the logging mechanism to work, there must be (implicit) evidence of _Loggable[X]_ available.
The following standard Loggables are provided:

    implicit object LoggableBoolean extends Loggable[Boolean]
    implicit object LoggableByte extends Loggable[Byte]
    implicit object LoggableShort extends Loggable[Short]
    implicit object LoggableInt extends Loggable[Int]
    implicit object LoggableLong extends Loggable[Long]
    implicit object LoggableBigInt extends Loggable[BigInt]
    implicit object LoggableString extends Loggable[String]
    implicit object LoggableDouble extends Loggable[Double]
    implicit object LoggableBigDecimal extends Loggable[BigDecimal]
    implicit object LoggableIterableAny extends LoggableIterable[Any]
    implicit object LoggableUnit extends Loggable[Unit]

Additionally, there is support, in _Loggables_, for containers to be logged.
The following are supported where, in each case, the parametric type T expects implicit evidence of type _Loggable[T]_:

    List[T]
    Vector[T]
    Map[K, T]
    Option[T]
    Either[L, R] (where each of L and R are Loggable
    Try[T]
    and _Product_ type up to _Product5_ (case classes and tuples) where each member type is _Loggable_.

### Variations
It is possible to change the default logger function by making an implicit value of _LogFunction_ available
prior to the import of _Flog.__
The default logger function uses _org.slf4j.LoggerFactory.getLogger_ to provide a logger.

## Dependencies
For the default logging function, we include the following dependencies:

    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
    "ch.qos.logback" % "logback-classic" % "1.2.3" % "runtime"

If you choose to use a different logger function, you may need to change these dependencies.

# Version
1.0.1 This project was cloned from DecisionTree.