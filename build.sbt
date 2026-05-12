ThisBuild / organization := "io.github.andy327"
ThisBuild / homepage := Some(url("https://github.com/andy327/kafka-event-lens"))
ThisBuild / description := "Kafka event streaming — producer, Kafka Streams processor, and consumer with Avro and Schema Registry"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / versionScheme := Some("early-semver")
ThisBuild / scalaVersion := "2.13.12"
ThisBuild / scalacOptions ++= Seq("-deprecation", "-Wunused", "-Wunused:imports")
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

// Suppress -Wunused warnings in the sbt console so REPL use isn't noisy
lazy val noUnusedInConsoles = {
  def dropUnused(opts: Seq[String]) =
    opts.filterNot(o => o.startsWith("-Wunused") || o.startsWith("-Ywarn-unused"))
  Seq(
    Compile / console / scalacOptions := dropUnused((Compile / console / scalacOptions).value),
    Test / console / scalacOptions := dropUnused((Test / console / scalacOptions).value),
  )
}

val kafkaVersion = "3.7.0"
val avro4sVersion = "4.1.2"
val confluentVersion = "7.6.0"
val logbackVersion = "1.4.14"

lazy val commonSettings = Seq(
  resolvers += "Confluent Maven" at "https://packages.confluent.io/maven/",
  libraryDependencies += "ch.qos.logback" % "logback-classic" % logbackVersion,
)

lazy val common = project
  .in(file("common"))
  .settings(
    commonSettings,
    noUnusedInConsoles,
    name := "common",
    libraryDependencies ++= Seq(
      "org.apache.kafka" % "kafka-clients" % kafkaVersion,
      "com.sksamuel.avro4s" %% "avro4s-core" % avro4sVersion,
      "io.confluent" % "kafka-schema-registry-client" % confluentVersion,
      "io.confluent" % "kafka-avro-serializer" % confluentVersion,
    )
  )

lazy val producer = project
  .in(file("producer"))
  .dependsOn(common)
  .settings(
    commonSettings,
    noUnusedInConsoles,
    name := "producer",
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case _                             => MergeStrategy.first
    },
  )

lazy val processor = project
  .in(file("processor"))
  .dependsOn(common)
  .settings(
    commonSettings,
    noUnusedInConsoles,
    name := "processor",
    libraryDependencies ++= Seq(
      "org.apache.kafka" % "kafka-streams" % kafkaVersion,
      "org.apache.kafka" %% "kafka-streams-scala" % kafkaVersion,
    ),
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case _                             => MergeStrategy.first
    },
  )

lazy val consumer = project
  .in(file("consumer"))
  .dependsOn(common)
  .settings(
    commonSettings,
    noUnusedInConsoles,
    name := "consumer",
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case _                             => MergeStrategy.first
    },
  )

lazy val root = project
  .in(file("."))
  .aggregate(common, producer, processor, consumer)
  .settings(
    name := "kafka-event-lens",
    assembly / aggregate := false,
  )
