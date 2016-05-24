import sbt.Keys._
import sbt._

object ApplicationBuild extends Build {

  val testDependencies = Seq (
    "org.scalatest" %% "scalatest" % "2.2.6" % "test",
    "junit" % "junit" % "4.12" % "test"
  )  

  val dependencies = Seq(
    "org.mongodb.scala" %% "mongo-scala-driver" % "1.1.1"
  ) ++ testDependencies

  val mongodbScalaDriverTest = Project(id = "mongodbScalaDriverTest", base = file("."))
    .settings(libraryDependencies ++= dependencies)

}    