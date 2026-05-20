package com.andy327.kl.common.serde

import java.util.{Map => JMap}

import scala.jdk.CollectionConverters._

import org.apache.kafka.common.serialization.{Deserializer, Serde, Serdes, Serializer}

import io.confluent.kafka.serializers.{AbstractKafkaSchemaSerDeConfig, KafkaAvroDeserializer, KafkaAvroSerializer}

import com.sksamuel.avro4s.RecordFormat

import org.apache.avro.generic.GenericRecord

import com.andy327.kl.common.model.{ActivityEvent, EnrichedEvent}

/** Avro-based `Serde` instances for pipeline domain types, backed by Confluent Schema Registry. */
object AvroSerdes {

  /** Returns a `Serde[ActivityEvent]` configured to use Confluent Schema Registry for Avro serialization.
    *
    * @param schemaRegistryUrl URL of the Confluent Schema Registry
    */
  def activityEvent(schemaRegistryUrl: String): Serde[ActivityEvent] = {
    val configs = Map(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG -> schemaRegistryUrl).asJava
    val ser = new ActivityEventSerializer
    val de = new ActivityEventDeserializer
    ser.configure(configs, false)
    de.configure(configs, false)
    Serdes.serdeFrom(ser, de)
  }

  /** Returns a `Serde[EnrichedEvent]` configured to use Confluent Schema Registry for Avro serialization.
    *
    * @param schemaRegistryUrl URL of the Confluent Schema Registry
    */
  def enrichedEvent(schemaRegistryUrl: String): Serde[EnrichedEvent] = {
    val configs = Map(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG -> schemaRegistryUrl).asJava
    val ser = new EnrichedEventSerializer
    val de = new EnrichedEventDeserializer
    ser.configure(configs, false)
    de.configure(configs, false)
    Serdes.serdeFrom(ser, de)
  }

  private class ActivityEventSerializer extends Serializer[ActivityEvent] {
    private val inner = new KafkaAvroSerializer()
    private val format = RecordFormat[ActivityEvent]

    override def configure(configs: JMap[String, _], isKey: Boolean): Unit = inner.configure(configs, isKey)
    override def serialize(topic: String, data: ActivityEvent): Array[Byte] =
      if (data == null) null else inner.serialize(topic, format.to(data))
    override def close(): Unit = inner.close()
  }

  private class ActivityEventDeserializer extends Deserializer[ActivityEvent] {
    private val inner = new KafkaAvroDeserializer()
    private val format = RecordFormat[ActivityEvent]

    override def configure(configs: JMap[String, _], isKey: Boolean): Unit = inner.configure(configs, isKey)
    override def deserialize(topic: String, data: Array[Byte]): ActivityEvent =
      if (data == null) null else format.from(inner.deserialize(topic, data).asInstanceOf[GenericRecord])
    override def close(): Unit = inner.close()
  }

  private class EnrichedEventSerializer extends Serializer[EnrichedEvent] {
    private val inner = new KafkaAvroSerializer()
    private val format = RecordFormat[EnrichedEvent]

    override def configure(configs: JMap[String, _], isKey: Boolean): Unit = inner.configure(configs, isKey)
    override def serialize(topic: String, data: EnrichedEvent): Array[Byte] =
      if (data == null) null else inner.serialize(topic, format.to(data))
    override def close(): Unit = inner.close()
  }

  private class EnrichedEventDeserializer extends Deserializer[EnrichedEvent] {
    private val inner = new KafkaAvroDeserializer()
    private val format = RecordFormat[EnrichedEvent]

    override def configure(configs: JMap[String, _], isKey: Boolean): Unit = inner.configure(configs, isKey)
    override def deserialize(topic: String, data: Array[Byte]): EnrichedEvent =
      if (data == null) null else format.from(inner.deserialize(topic, data).asInstanceOf[GenericRecord])
    override def close(): Unit = inner.close()
  }
}
