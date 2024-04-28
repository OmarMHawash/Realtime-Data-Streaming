ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.18"

libraryDependencies += "org.apache.spark" %% "spark-core" % "3.5.0"
libraryDependencies += "org.apache.spark" %% "spark-streaming" % "3.5.0"
libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka-0-10" % "3.5.0"
libraryDependencies += "org.apache.kafka" %% "kafka" % "3.6.0"
libraryDependencies += "org.mongodb.scala" %% "mongo-scala-driver" % "4.11.0"

lazy val root = (project in file("."))
  .settings(
    name := "tweetsStreaming"
  )
