organization := "com.phasmidsoftware"

name := "flog"

version := "1.0.15"

scalaVersion := "3.9.0" // the current LTS

Compile / scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Wunused:all"   // warns on unused imports, params, givens etc.
)

val scalaTestVersion = "3.2.20"
val logBackVersion = "1.5.37"
val slf4jVersion = "2.0.18"

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % slf4jVersion,
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
  "ch.qos.logback" % "logback-classic" % logBackVersion % "test"
)

// JUnit-style reports, collected by CircleCI
Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-u", "target/test-reports")

// Scaladoc is published to https://rchillyard.github.io/Flog/latest/api/ by `sbt ghpagesPushSite`
enablePlugins(GhpagesPlugin, SiteScaladocPlugin)
git.remoteRepo := "git@github.com:rchillyard/Flog.git"
