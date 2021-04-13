// Flog examples

import com.phasmidsoftware.flog.Flog._
import com.phasmidsoftware.flog.{Loggable, Loggables}

// The following should yield the value "World"
// while creating something like the following log entry:
// <datetime> DEBUG c.phasmidsoftware.flog.Flog$Flogger  - log: Hello: World
"Hello" !! "World"

// The following should yield the value List(1, 2, 3)
// while creating something like the following log entry:
// <datetime> DEBUG c.phasmidsoftware.flog.Flog$Flogger  - log: test lists: [1, ... (1 elements), ... 3]
implicit val listLoggable: Loggable[List[Int]] = new Loggables {}.listLoggable[Int]
"test lists" !! List(1, 2, 3)

// The following should yield the value List(1, 2, 3)
// while creating something like the following log entry:
// <datetime> DEBUG c.phasmidsoftware.flog.Flog$Flogger  - log: test complex: Complex(real:1.0,imag:0.0)
case class Complex(real: Double, imag: Double)
implicit val complexLoggable: Loggable[Complex] = new Loggables {}.toLog2(Complex)
"test complex" !! Complex(1, 0)
