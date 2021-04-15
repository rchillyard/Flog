// Flog examples

import com.phasmidsoftware.flog.Flog._
import com.phasmidsoftware.flog.Loggable._
import com.phasmidsoftware.flog.{Loggable, Loggables}
import scala.concurrent.Future
import scala.util.Try

// The following should yield the value: "World"
// while creating something like the following log entry:
// <datetime> DEBUG c.phasmidsoftware.flog.Flog$Flogger  - log: Hello: World
"Hello World" !! ()

// The following should yield the value: "World"
// while creating something like the following log entry:
// <datetime> DEBUG c.phasmidsoftware.flog.Flog$Flogger  - log: Hello: World
"Hello" !! "World"

// The following should yield the value: Some(42)
// while creating something like the following log entry:
// <datetime> DEBUG c.phasmidsoftware.flog.Flog$Flogger  - log: test Option: Some(42)
// You don't need to include an implicit Loggable[Option[Int]] because there is one imported from Loggable._
"test Option" !! Option(42)

// The following should yield the value: List(1, 2, 3)
// while creating something like the following log entry:
// <datetime> DEBUG c.phasmidsoftware.flog.Flog$Flogger  - log: test Iterable: [1, ... (1 elements), ... 3]
// NOTE: any Iterable should match the !| method.
// If you want to use only the !! method, then you will need to do something like the block of code following, with
// the explicit implicit val.
"test Iterable" !! Seq(1, 2, 3, 4)

// The following should yield the value: Seq(1, 2, 3)
// while creating something like the following log entry:
// <datetime> DEBUG c.phasmidsoftware.flog.Flog$Flogger  - log: test Seq: [1, ... (1 elements), ... 3]
implicit val seqLoggable: Loggable[Seq[Int]] = new Loggables {}.seqLoggable[Int]
"test Seq" !! Seq(1, 2, 3, 4)

// The following should yield the value: Success(42)
// while creating something like the following log entry:
// <datetime> DEBUG c.phasmidsoftware.flog.Flog$Flogger  - log: test Try: Success(42)
implicit val tryLoggable: Loggable[Try[Int]] = new Loggables {}.tryLoggable[Int]
"test Try" !! Try(42)

// The following should yield the value: Complex(1.0,0.0)
// while creating something like the following log entry:
// <datetime> DEBUG c.phasmidsoftware.flog.Flog$Flogger  - log: test complex: Complex(real:1.0,imag:0.0)
case class Complex(real: Double, imag: Double)

implicit val complexLoggable: Loggable[Complex] = new Loggables {}.loggable2(Complex)
"test Complex" !! Complex(1, 0)

// The following should yield the value: Future(Success("hello"))
// while creating something like the following TWO log entries:
// <datetime> DEBUG c.phasmidsoftware.flog.Flog$Flogger  - log: test Future: Future: promise (0008b203-cfa7-4233-9fd1-84b43069fa8d) created...
// <datetime> DEBUG c.phasmidsoftware.flog.Flog$Flogger  - log: Future completed (0008b203-cfa7-4233-9fd1-84b43069fa8d): Success(hello)

import scala.concurrent.ExecutionContext.Implicits.global

implicit val futureLoggable: Loggable[Future[String]] = new Loggables {}.futureLoggable[String]
"test Future" !! Future("hello")

// The following does not compile because we don't currently declare
// an implicit value of Loggable[LocalDateTime].
// If you uncomment the following line, you will see the compiler message that is generated.
//"now" !! LocalDateTime.now()