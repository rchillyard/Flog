organization := "com.phasmidsoftware"

name := "flog"

version := "1.0.9-SNAPSHOT"

scalaVersion := "2.13.10"

scalacOptions += "-deprecation"

val scalaTestVersion = "3.2.14"
val logBackVersion = "1.4.5"
val slf4jVersion = "2.0.5"

Compile / scalacOptions ++= Seq("-deprecation")

resolvers += "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % slf4jVersion,
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
  "ch.qos.logback" % "logback-classic" % logBackVersion % "test"
)
