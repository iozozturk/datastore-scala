name := "datastore-scala"

version := "0.1.3"

scalaVersion := "2.13.0"

lazy val root = (project in file("."))
  .enablePlugins(JavaAgent, AkkaGrpcPlugin, JavaAppPackaging, DockerPlugin, AshScriptPlugin, DockerCompose)
  .configs(IntegrationTest)
  .settings(
    Defaults.itSettings,
    javaAgents += "org.mortbay.jetty.alpn" % "jetty-alpn-agent" % "2.0.9" % "compile"
  )

val akkaVersion = "2.5.23"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "io.grpc" % "grpc-auth" % "1.23.0",
  "com.google.auth" % "google-auth-library-oauth2-http" % "0.17.0",
  "com.google.api.grpc" % "proto-google-cloud-datastore-v1" % "0.70.0" % "protobuf",
  "org.scalatest" %% "scalatest" % "3.0.8" % "it,test",
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % "it,test",
  "org.mockito" % "mockito-core" % "3.0.0" % Test
)

useGpg := true

ThisBuild / organization := "com.ismetozozturk"
ThisBuild / organizationName := "ismetozozturk"
ThisBuild / organizationHomepage := Some(url("https://ismetozozturk.com/"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/iozozturk/datastore-scala.git"),
    "scm:git@github.com:iozozturk/datastore-scala.git"
  )
)
ThisBuild / developers := List(
  Developer(
    id    = "iozozturk",
    name  = "Ismet Ozozturk",
    email = "iozozturk@gmail.com",
    url   = url("https://ismetozozturk.com")
  )
)

ThisBuild / description := "Google Cloud Datastore client library"
ThisBuild / licenses := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / homepage := Some(url("https://github.com/iozozturk/datastore-scala"))

// Remove all additional repository other than Maven Central from POM
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
ThisBuild / publishMavenStyle := true