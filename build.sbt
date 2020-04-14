scalaVersion := "2.12.7"

organization := "com.bombsquad"
name := "bombsqad-api"
version := "1.0"
maintainer := "raul.bajales@gmail.com"

mainClass in Compile := Some("com.bombsquad.QuickstartApp")

herokuJdkVersion in Compile := "11"
herokuAppName in Compile := "bombsquad-api"

lazy val akkaHttpVersion = "10.1.11"
lazy val akkaVersion = "2.6.4"

enablePlugins(JavaAppPackaging)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "com.typesafe" % "config" % "1.4.0",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.mongodb.scala" %% "mongo-scala-driver" % "4.0.2",

  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.0.8" % Test
)