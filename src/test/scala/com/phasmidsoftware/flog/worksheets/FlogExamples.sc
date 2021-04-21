// Flog examples

import com.phasmidsoftware.flog.Loggable._
import com.phasmidsoftware.flog.{Flog, Loggable, Loggables}

import java.time.LocalDateTime
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

// The following should yield the value: List(1, 2, 3, 4, 5)
// while creating something like the following log entry:
// <datetime> DEBUG c.phasmidsoftware.flog.Flog$Flogger  - Flog: test Iterable: {1, 2, ... (1 element), ... 5}
"test Iterable" !! Seq(1, 2, 3, 4, 5)

// You can log any type if you use the !| method instead of !!
// But you have no control over the appearance of the object to be logged.
"test any type" !| LocalDateTime.now

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
