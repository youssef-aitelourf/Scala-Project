ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.5"

lazy val root = (project in file("."))
  .settings(
    name := "Project"
  )

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "3.5.2",
  "org.scalafx" %% "scalafx" % "21.0.0-R32",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.5.1", // optional: for connection pooling
  "com.h2database" % "h2" % "2.3.232"

)