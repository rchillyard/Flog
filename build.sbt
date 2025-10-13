organization := "com.phasmidsoftware"

name := "flog"

version := "1.0.10"

scalaVersion := "3.7.3"

Compile / scalacOptions ++= Seq("-deprecation")

val scalaTestVersion = "3.2.19"
val logBackVersion = "1.5.19"
val slf4jVersion = "2.0.17"

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % slf4jVersion,
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
  "ch.qos.logback" % "logback-classic" % logBackVersion % "test"
)

resolvers += "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/"

