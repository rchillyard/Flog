// Flog examples

import com.phasmidsoftware.flog.Flog._
import com.phasmidsoftware.flog.{Loggable, Loggables}
import scala.concurrent.Future

// The following should yield the value: "World"
// while creating something like the following log entry:
// <datetime> DEBUG c.phasmidsoftware.flog.Flog$Flogger  - log: Hello: World
"Hello" !! "World"

// The following should yield the value: List(1, 2, 3)
// while creating something like the following log entry:
// <datetime> DEBUG c.phasmidsoftware.flog.Flog$Flogger  - log: test lists: [1, ... (1 elements), ... 3]
implicit val listLoggable: Loggable[List[Int]] = new Loggables {}.listLoggable[Int]
"test lists" !! List(1, 2, 3)

// The following should yield the value: Complex(1.0,0.0)
// while creating something like the following log entry:
// <datetime> DEBUG c.phasmidsoftware.flog.Flog$Flogger  - log: test complex: Complex(real:1.0,imag:0.0)
case class Complex(real: Double, imag: Double)
implicit val complexLoggable: Loggable[Complex] = new Loggables {}.toLog2(Complex)
"test complex" !! Complex(1, 0)

// The following should yield the value: Future(Success("hello"))
// while creating something like the following TWO log entries:
// <datetime> DEBUG c.phasmidsoftware.flog.Flog$Flogger  - log: test future: Future: promise (0008b203-cfa7-4233-9fd1-84b43069fa8d) created...
// <datetime> DEBUG c.phasmidsoftware.flog.Flog$Flogger  - log: Future completed (0008b203-cfa7-4233-9fd1-84b43069fa8d): Success(hello)
import scala.concurrent.ExecutionContext.Implicits.global
implicit val futureLoggable: Loggable[Future[String]] = new Loggables {}.futureLoggable[String]
"test future" !! Future("hello")
