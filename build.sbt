organization := "com.phasmidsoftware"

name := "DecisionTree"

version := "1.0.1-SNAPSHOT"

scalaVersion := "2.12.9"

val scalaTestVersion = "3.0.5"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
  "ch.qos.logback" % "logback-classic" % "1.2.3" % "runtime",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
)
