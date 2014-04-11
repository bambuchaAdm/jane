organization := "pl.hackerspace-krk"

name := "watson"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.2",
  "ch.qos.logback" % "logback-core" % "1.1.1",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.2" % "test",
  "org.scalatest" % "scalatest_2.10" % "2.1.3" % "test"
)
