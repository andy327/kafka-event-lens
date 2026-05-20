package com.andy327.kl.processor

import org.apache.kafka.streams.KafkaStreams

import com.andy327.kl.common.config.KafkaConfig

/** Entry point for the Kafka Streams processor application.
  *
  * Reads `BOOTSTRAP_SERVERS` and `SCHEMA_REGISTRY_URL` from the environment, falling back to localhost defaults
  * suitable for the Docker Compose development stack.
  */
object Main extends App {

  private val bootstrapServers = sys.env.getOrElse("BOOTSTRAP_SERVERS", "localhost:9092")
  private val schemaRegistryUrl = sys.env.getOrElse("SCHEMA_REGISTRY_URL", "http://localhost:8081")

  private val props = KafkaConfig.streamsProps(bootstrapServers, schemaRegistryUrl, KafkaConfig.GroupIds.Processor)
  private val topology = Topology.build(schemaRegistryUrl)
  private val streams = new KafkaStreams(topology, props)

  sys.addShutdownHook(streams.close())

  streams.start()
}
