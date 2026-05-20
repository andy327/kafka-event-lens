package com.andy327.kl.processor

import org.apache.kafka.common.serialization.Serdes.StringSerde
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.kstream.{Consumed, Produced}
import org.apache.kafka.streams.{Topology => KafkaTopology}

import com.andy327.kl.common.config.KafkaConfig
import com.andy327.kl.common.serde.AvroSerdes

/** Builds the Kafka Streams topology for the activity event processing pipeline. */
object Topology {

  /** Constructs and returns the processing topology.
    *
    * Reads `ActivityEvent`s from the raw topic, routes valid events through enrichment to `user-activity-enriched`,
    * sends invalid events to `user-activity-dlq`, and attaches a windowed event-count aggregation by business and type.
    *
    * @param schemaRegistryUrl URL of the Confluent Schema Registry used to configure Avro Serdes
    */
  def build(schemaRegistryUrl: String): KafkaTopology = {
    val builder = new StreamsBuilder()

    val stringSerde = new StringSerde()
    val activitySerde = AvroSerdes.activityEvent(schemaRegistryUrl)
    val enrichedSerde = AvroSerdes.enrichedEvent(schemaRegistryUrl)

    val stream = builder.stream(KafkaConfig.Topics.UserActivityRaw)(
      Consumed.`with`(stringSerde, activitySerde)
    )

    val validStream = stream.filter((_, event) => EventValidator.isValid(event))
    val invalidStream = stream.filterNot((_, event) => EventValidator.isValid(event))

    validStream
      .mapValues(event => EventEnricher.enrich(event))
      .to(KafkaConfig.Topics.UserActivityEnriched)(Produced.`with`(stringSerde, enrichedSerde))

    invalidStream
      .to(KafkaConfig.Topics.UserActivityDlq)(Produced.`with`(stringSerde, activitySerde))

    StatsAggregator.buildStats(validStream, activitySerde)

    builder.build()
  }
}
