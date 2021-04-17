// Flog examples

import com.phasmidsoftware.flog.Loggable._
import com.phasmidsoftware.flog.{Flog, Loggable, Loggables}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

val flog: Flog = Flog()

import flog._

// The following should yield the value: "World"
// while creating something like the following log entry:
// <datetime> DEBUG c.phasmidsoftware.flog.Flog$Flogger  - Flog: Hello: World
"Hello World" !! ()

// The following should yield the value: "World"
// while creating something like the following log entry:
// <datetime> DEBUG c.phasmidsoftware.flog.Flog$Flogger  - Flog: Hello: World
val x = "Hello" !! "World"

// The following should yield the value: Some(42)
// while creating something like the following log entry:
// <datetime> DEBUG c.phasmidsoftware.flog.Flog$Flogger  - Flog: test Option: Some(42)
// You don't need to include an implicit Loggable[Option[Int]] because there is one imported from Loggable._
"test Option" !! Option(42)

// The following should yield the value: List(1, 2, 3, 4)
// while creating something like the following log entry:
// <datetime> DEBUG c.phasmidsoftware.flog.Flog$Flogger  - Flog: test Iterable: [1, 2, ... (1 elements), ... 4]
"test Iterable" !! Seq(1, 2, 3, 4)

// You can log any iterable provided that there is an implicit logger for the underlying type.
"test Iterable of String" !! Seq("1", "2", "3", "4")

// The following should yield the value: Seq(1, 2, 3, 4)
// while creating something like the following log entry:
// <datetime> DEBUG c.phasmidsoftware.flog.Flog$Flogger  - Flog: test Seq: [1, ... (1 elements), ... 3]
// This is essentially an alternative to using the Iterable logger (above).
implicit val seqLoggable: Loggable[Seq[Int]] = new Loggables {}.seqLoggable[Int]
"test Seq" !! Seq(1, 2, 3, 4)

// The following should yield the value: Success(42)
// while creating something like the following log entry:
// <datetime> DEBUG c.phasmidsoftware.flog.Flog$Flogger  - Flog: test Try: Success(42)
implicit val tryLoggable: Loggable[Try[Int]] = new Loggables {}.tryLoggable[Int]
"test Try" !! Try(42)

// The following should yield the value: Complex(1.0,0.0)
// while creating something like the following log entry:
// <datetime> DEBUG c.phasmidsoftware.flog.Flog$Flogger  - Flog: test complex: Complex(real:1.0,imag:0.0)
case class Complex(real: Double, imag: Double)

implicit val complexLoggable: Loggable[Complex] = new Loggables {}.loggable2(Complex)
"test Complex" !! Complex(1, 0)

// The following should yield the value: Future(Success("hello"))
// while creating something like the following TWO log entries:
// Note: they might come out in the wrong order (or the completed might be missing entirely).
// <datetime> DEBUG c.phasmidsoftware.flog.Flog$Flogger  - Flog: test Future: Future: promise (0008b203-cfa7-4233-9fd1-84b43069fa8d) created...
// <datetime> DEBUG c.phasmidsoftware.flog.Flog$Flogger  - Future completed (0008b203-cfa7-4233-9fd1-84b43069fa8d): Success(hello)
implicit val futureLoggable: Loggable[Future[String]] = new Loggables {}.futureLoggable[String]
"test Future" !! Future("hello")

Thread.sleep(500)
"finished"

// The following does not compile because we don't currently declare
// an implicit value of Loggable[LocalDateTime].
// If you uncomment the following line, you will see the compiler message that is generated.
//"now" !! LocalDateTime.now()
