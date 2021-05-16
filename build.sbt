organization := "com.phasmidsoftware"

name := "flog"

version := "1.0.7"

scalaVersion := "2.13.5"

scalacOptions += "-deprecation"

val scalaTestVersion = "3.2.7"
val logBackVersion = "1.2.3"
val slf4jVersion = "1.7.30"

scalacOptions in (Compile,doc) ++= Seq("-groups", "-implicits", "-deprecation")

resolvers += "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % slf4jVersion,
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
  "ch.qos.logback" % "logback-classic" % logBackVersion % "test"
)
