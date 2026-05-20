package com.andy327.kl.processor

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

import com.andy327.kl.common.model.{ActivityEvent, EventType, ReviewPayload}

final class EventValidatorSpec extends AnyWordSpec {

  private val validEvent = ActivityEvent(
    eventId = "evt-1",
    userId = "user-001",
    businessId = "business-001",
    eventType = EventType.ReviewSubmitted,
    timestampMs = 1_000_000L,
    payload = ReviewPayload(rating = 4, reviewText = "Great!")
  )

  "EventValidator" should {
    "accept a well-formed event" in {
      EventValidator.isValid(validEvent) shouldBe true
    }

    "reject an event with an empty eventId" in {
      EventValidator.isValid(validEvent.copy(eventId = "")) shouldBe false
    }

    "reject an event with an empty userId" in {
      EventValidator.isValid(validEvent.copy(userId = "")) shouldBe false
    }

    "reject an event with an empty businessId" in {
      EventValidator.isValid(validEvent.copy(businessId = "")) shouldBe false
    }

    "reject an event with a zero timestampMs" in {
      EventValidator.isValid(validEvent.copy(timestampMs = 0L)) shouldBe false
    }

    "reject an event with a negative timestampMs" in {
      EventValidator.isValid(validEvent.copy(timestampMs = -1L)) shouldBe false
    }

    "reject an event with an unknown eventType" in {
      EventValidator.isValid(validEvent.copy(eventType = "UNKNOWN_TYPE")) shouldBe false
    }

    "accept all four known event types" in
      EventType.all.foreach { eventType =>
        EventValidator.isValid(validEvent.copy(eventType = eventType)) shouldBe true
      }
  }
}
