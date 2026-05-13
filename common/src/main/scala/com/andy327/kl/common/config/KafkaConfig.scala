package com.andy327.kl.common.config

import java.util.Properties

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

import io.confluent.kafka.serializers.{
  AbstractKafkaSchemaSerDeConfig,
  KafkaAvroDeserializer,
  KafkaAvroDeserializerConfig,
  KafkaAvroSerializer
}

/** Topic names, consumer group IDs, and `Properties` builders for all Kafka clients in this pipeline. */
object KafkaConfig {

  /** Kafka topic name constants for all topics in this pipeline. */
  object Topics {
    val UserActivityRaw = "user-activity-raw"
    val UserActivityEnriched = "user-activity-enriched"
    val UserActivityDlq = "user-activity-dlq"
    val BusinessStatsChangelog = "business-stats-changelog"

    val all: Seq[String] = Seq(UserActivityRaw, UserActivityEnriched, UserActivityDlq, BusinessStatsChangelog)
  }

  /** Consumer group ID constants for each application that reads from Kafka. */
  object GroupIds {
    val Processor = "kafka-lens-processor"
    val Consumer = "kafka-lens-consumer"
  }

  /** Properties for an Avro producer keyed by `String`.
    *
    * @param bootstrapServers comma-separated list of Kafka broker addresses
    * @param schemaRegistryUrl URL of the Confluent Schema Registry
    */
  def producerProps(bootstrapServers: String, schemaRegistryUrl: String): Properties = {
    val props = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[KafkaAvroSerializer].getName)
    props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl)
    props
  }

  /** Properties for an Avro consumer keyed by `String` with manual offset commit.
    *
    * @param bootstrapServers comma-separated list of Kafka broker addresses
    * @param schemaRegistryUrl URL of the Confluent Schema Registry
    * @param groupId consumer group ID for this application
    */
  def consumerProps(bootstrapServers: String, schemaRegistryUrl: String, groupId: String): Properties = {
    val props = new Properties()
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[KafkaAvroDeserializer].getName)
    props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl)
    props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true")
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    props
  }

  /** Properties for a Kafka Streams application using Avro.
    *
    * @param bootstrapServers comma-separated list of Kafka broker addresses
    * @param schemaRegistryUrl URL of the Confluent Schema Registry
    * @param applicationId unique identifier for this Kafka Streams application
    */
  def streamsProps(bootstrapServers: String, schemaRegistryUrl: String, applicationId: String): Properties = {
    val props = new Properties()
    props.put("application.id", applicationId)
    props.put("bootstrap.servers", bootstrapServers)
    props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl)
    props
  }
}
