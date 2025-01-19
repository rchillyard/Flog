organization := "com.phasmidsoftware"

name := "flog"

version := "1.0.10"

scalaVersion := "2.13.16"

scalacOptions += "-deprecation"

val scalaTestVersion = "3.2.19"
val logBackVersion = "1.4.12"
val slf4jVersion = "2.0.16"

Compile / scalacOptions ++= Seq("-deprecation")

resolvers += "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % slf4jVersion,
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
  "ch.qos.logback" % "logback-classic" % logBackVersion % "test"
)
