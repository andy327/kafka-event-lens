package com.andy327.kl.producer

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.serialization.StringSerializer

import com.andy327.kl.common.config.{KafkaConfig, TopicAdmin}
import com.andy327.kl.common.model.ActivityEvent
import com.andy327.kl.common.serde.AvroSerdes

/** Entry point for the synthetic event producer.
  *
  * Reads `BOOTSTRAP_SERVERS` and `SCHEMA_REGISTRY_URL` from the environment, falling back to localhost defaults
  * suitable for the Docker Compose development stack.
  */
object Main extends App {

  private val bootstrapServers = sys.env.getOrElse("BOOTSTRAP_SERVERS", "localhost:9092")
  private val schemaRegistryUrl = sys.env.getOrElse("SCHEMA_REGISTRY_URL", "http://localhost:8081")

  private val adminClient = AdminClient.create(KafkaConfig.producerProps(bootstrapServers, schemaRegistryUrl))
  TopicAdmin.createAll(adminClient)
  adminClient.close()

  private val serde = AvroSerdes.activityEvent(schemaRegistryUrl)
  private val kafkaProducer = new KafkaProducer[String, ActivityEvent](
    KafkaConfig.producerProps(bootstrapServers, schemaRegistryUrl),
    new StringSerializer(),
    serde.serializer()
  )
  private val producer = new ActivityProducer(kafkaProducer)

  sys.addShutdownHook(producer.close())

  producer.run(new EventGenerator().stream())
}
