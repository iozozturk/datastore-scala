name := "datastore-scala"

version := "0.1.2"

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

resolvers += GCSResolver.forBucket("library.artifacts.newmotion.dev")

publishTo := Some(GCSPublisher.forBucket("library.artifacts.newmotion.dev", AccessRights.InheritBucket))