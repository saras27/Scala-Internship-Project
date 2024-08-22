name := """social-network"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.14"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.0" % Test

libraryDependencies ++= Seq(jdbc,
  "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0" % Test
)

libraryDependencies ++= Seq(
  "com.mysql" % "mysql-connector-j" % "8.0.33"
)

libraryDependencies += "com.pauldijou" %% "jwt-play" % "5.0.0" exclude("org.scala-lang.modules", "scala-xml_2.13")
libraryDependencies += "com.pauldijou" %% "jwt-core" % "5.0.0"

libraryDependencies += "org.mindrot" % "jbcrypt" % "0.4"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-mailer" % "8.0.1" ,
  "com.typesafe.play" %% "play-mailer-guice" % "8.0.1"
)

libraryDependencies ++= Seq(evolutions, jdbc)
libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "5.0.0" exclude("org.scala-lang.modules", "scala-xml_2.13"),
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.auth0" % "jwks-rsa" % "0.6.1"
)
// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"
