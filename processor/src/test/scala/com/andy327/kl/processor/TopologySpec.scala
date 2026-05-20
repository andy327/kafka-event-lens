package com.andy327.kl.processor

import java.util.Properties

import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.apache.kafka.streams.{StreamsConfig, TopologyTestDriver}

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

import com.andy327.kl.common.config.KafkaConfig
import com.andy327.kl.common.model._
import com.andy327.kl.common.serde.AvroSerdes

final class TopologySpec extends AnyWordSpec {

  private val mockRegistryUrl = "mock://test"

  private val activitySerde = AvroSerdes.activityEvent(mockRegistryUrl)
  private val enrichedSerde = AvroSerdes.enrichedEvent(mockRegistryUrl)

  private def makeDriver(): TopologyTestDriver = {
    val props = new Properties()
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "topology-test")
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:9092")
    new TopologyTestDriver(Topology.build(mockRegistryUrl), props)
  }

  private val validEvent = ActivityEvent(
    eventId = "evt-1",
    userId = "user-001",
    businessId = "business-001",
    eventType = EventType.ReviewSubmitted,
    timestampMs = 1_000_000L,
    payload = ReviewPayload(rating = 4, reviewText = "Great!")
  )

  private val invalidEvent = validEvent.copy(eventId = "")

  "Topology" should {
    "route a valid event to the enriched topic" in {
      val driver = makeDriver()
      val input =
        driver.createInputTopic(KafkaConfig.Topics.UserActivityRaw, new StringSerializer(), activitySerde.serializer())
      val output = driver.createOutputTopic(
        KafkaConfig.Topics.UserActivityEnriched,
        new StringDeserializer(),
        enrichedSerde.deserializer()
      )

      input.pipeInput(validEvent.userId, validEvent)

      output.isEmpty shouldBe false
      val result = output.readValue()
      result.eventId shouldBe validEvent.eventId
      result.userId shouldBe validEvent.userId
      result.payloadType shouldBe "ReviewPayload"
      result.processedAt should be > 0L

      driver.close()
    }

    "route an invalid event to the DLQ topic" in {
      val driver = makeDriver()
      val input =
        driver.createInputTopic(KafkaConfig.Topics.UserActivityRaw, new StringSerializer(), activitySerde.serializer())
      val dlq = driver.createOutputTopic(
        KafkaConfig.Topics.UserActivityDlq,
        new StringDeserializer(),
        activitySerde.deserializer()
      )

      input.pipeInput(invalidEvent.userId, invalidEvent)

      dlq.isEmpty shouldBe false
      dlq.readValue().eventId shouldBe invalidEvent.eventId

      driver.close()
    }

    "not route a valid event to the DLQ topic" in {
      val driver = makeDriver()
      val input =
        driver.createInputTopic(KafkaConfig.Topics.UserActivityRaw, new StringSerializer(), activitySerde.serializer())
      val dlq = driver.createOutputTopic(
        KafkaConfig.Topics.UserActivityDlq,
        new StringDeserializer(),
        activitySerde.deserializer()
      )

      input.pipeInput(validEvent.userId, validEvent)

      dlq.isEmpty shouldBe true

      driver.close()
    }

    "not route an invalid event to the enriched topic" in {
      val driver = makeDriver()
      val input =
        driver.createInputTopic(KafkaConfig.Topics.UserActivityRaw, new StringSerializer(), activitySerde.serializer())
      val output = driver.createOutputTopic(
        KafkaConfig.Topics.UserActivityEnriched,
        new StringDeserializer(),
        enrichedSerde.deserializer()
      )

      input.pipeInput(invalidEvent.userId, invalidEvent)

      output.isEmpty shouldBe true

      driver.close()
    }
  }
}
