organization := "com.phasmidsoftware"

name := "flog"

version := "1.0.2"

scalaVersion := "2.13.5"

scalacOptions += "-deprecation"

val scalaTestVersion = "3.1.1"

resolvers += "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
  "ch.qos.logback" % "logback-classic" % "1.2.3" % "runtime"
)
libraryDependencies ++= Seq("org.slf4j" % "slf4j-api" % "1.7.5" % "test",
  "org.slf4j" % "slf4j-simple" % "1.7.5" % "test")
