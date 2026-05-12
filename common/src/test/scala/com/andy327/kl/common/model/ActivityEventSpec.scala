package com.andy327.kl.common.model

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

final class ActivityEventSpec extends AnyWordSpec {

  private val sampleEvent = ActivityEvent(
    eventId = "evt-1",
    userId = "user-42",
    businessId = "biz-7",
    eventType = EventType.ReviewSubmitted,
    timestampMs = 1_000_000L,
    payload = ReviewPayload(rating = 4, reviewText = "Great place!")
  )

  "ActivityEvent" should {
    "hold the expected field values" in {
      sampleEvent.eventId shouldBe "evt-1"
      sampleEvent.userId shouldBe "user-42"
      sampleEvent.businessId shouldBe "biz-7"
      sampleEvent.eventType shouldBe EventType.ReviewSubmitted
    }

    "support copy with modified fields" in {
      val updated = sampleEvent.copy(userId = "user-99")
      updated.userId shouldBe "user-99"
      updated.businessId shouldBe sampleEvent.businessId
    }
  }

  "EventType" should {
    "define exactly four event types" in {
      EventType.all should have size 4
    }

    "contain all expected type constants" in
      (EventType.all should contain).allOf(
        EventType.ReviewSubmitted,
        EventType.PhotoUploaded,
        EventType.BusinessViewed,
        EventType.CheckIn
      )
  }

  "EventPayload" should {
    "pattern match exhaustively across all subtypes" in {
      def describe(p: EventPayload): String = p match {
        case ReviewPayload(r, _)  => s"review: $r stars"
        case PhotoPayload(url)    => s"photo: $url"
        case ViewPayload(ref, ms) => s"view: $ref $ms"
        case CheckInPayload       => "check-in"
      }

      describe(ReviewPayload(5, "Amazing!")) shouldBe "review: 5 stars"
      describe(PhotoPayload("https://example.com/1")) shouldBe "photo: https://example.com/1"
      describe(ViewPayload(Some("search"), Some(3000L))) shouldBe "view: Some(search) Some(3000)"
      describe(CheckInPayload) shouldBe "check-in"
    }
  }

  "ReviewPayload" should {
    "hold rating and review text" in {
      val p = ReviewPayload(rating = 3, reviewText = "Decent")
      p.rating shouldBe 3
      p.reviewText shouldBe "Decent"
    }
  }

  "ViewPayload" should {
    "allow optional referrer and duration to be present or absent" in {
      val withBoth = ViewPayload(Some("search"), Some(5000L))
      val withNeither = ViewPayload(None, None)

      withBoth.referrer shouldBe Some("search")
      withBoth.durationMs shouldBe Some(5000L)
      withNeither.referrer shouldBe None
      withNeither.durationMs shouldBe None
    }
  }
}
