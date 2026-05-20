package com.andy327.kl.processor

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

import com.andy327.kl.common.model._

final class EventEnricherSpec extends AnyWordSpec {

  private val baseEvent = ActivityEvent(
    eventId = "evt-1",
    userId = "user-001",
    businessId = "business-001",
    eventType = EventType.ReviewSubmitted,
    timestampMs = 1_000_000L,
    payload = ReviewPayload(rating = 4, reviewText = "Great!")
  )

  "EventEnricher" should {
    "preserve all fields from the source event" in {
      val enriched = EventEnricher.enrich(baseEvent)
      enriched.eventId shouldBe baseEvent.eventId
      enriched.userId shouldBe baseEvent.userId
      enriched.businessId shouldBe baseEvent.businessId
      enriched.eventType shouldBe baseEvent.eventType
      enriched.timestampMs shouldBe baseEvent.timestampMs
      enriched.payload shouldBe baseEvent.payload
    }

    "set a positive processedAt timestamp" in {
      EventEnricher.enrich(baseEvent).processedAt should be > 0L
    }

    "set payloadType to ReviewPayload for a review event" in {
      EventEnricher.enrich(baseEvent).payloadType shouldBe "ReviewPayload"
    }

    "set payloadType to PhotoPayload for a photo event" in {
      val event =
        baseEvent.copy(eventType = EventType.PhotoUploaded, payload = PhotoPayload("https://example.com/photo.jpg"))
      EventEnricher.enrich(event).payloadType shouldBe "PhotoPayload"
    }

    "set payloadType to ViewPayload for a view event" in {
      val event =
        baseEvent.copy(eventType = EventType.BusinessViewed, payload = ViewPayload(Some("search"), Some(5000L)))
      EventEnricher.enrich(event).payloadType shouldBe "ViewPayload"
    }

    "set payloadType to CheckInPayload for a check-in event" in {
      val event = baseEvent.copy(eventType = EventType.CheckIn, payload = CheckInPayload)
      EventEnricher.enrich(event).payloadType shouldBe "CheckInPayload$"
    }
  }
}
