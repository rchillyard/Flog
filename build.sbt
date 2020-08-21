organization := "com.phasmidsoftware"

name := "DecisionTree"

version := "1.0.3-SNAPSHOT"

scalaVersion := "2.13.3"

scalacOptions += "-deprecation"

val scalaTestVersion = "3.1.1"

resolvers += "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
)

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.2.3" % "runtime",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
)
