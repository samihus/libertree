ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .settings(
    name := "Tree",
    idePackagePrefix := Some("com.datanarchi.libs.scala.trees")
  )

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % Test

