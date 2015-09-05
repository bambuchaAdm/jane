organization := "pl.hackerspace-krk"

name := "watson"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.6",
  "ch.qos.logback" % "logback-core" % "1.1.1",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.2" % "test",
  "org.scalatest" %% "scalatest" % "2.1.3" % "test",
  "org.mockito" % "mockito-core" % "1.9.5" % "test"
)
