package com.andy327.kl.producer

import scala.util.Random

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

import com.andy327.kl.common.model._

final class EventGeneratorSpec extends AnyWordSpec {

  private val seed = 42L
  private val fixedClock: () => Long = () => 1_000_000L
  private def seededGenerator = new EventGenerator(new Random(seed), fixedClock)
  private def sampleEvents(n: Int = 1000) = seededGenerator.stream().take(n).toList

  "EventGenerator" should {
    "produce events whose payload type matches their eventType" in
      sampleEvents().foreach { event =>
        event.eventType match {
          case EventType.ReviewSubmitted => event.payload shouldBe a[ReviewPayload]
          case EventType.PhotoUploaded   => event.payload shouldBe a[PhotoPayload]
          case EventType.BusinessViewed  => event.payload shouldBe a[ViewPayload]
          case EventType.CheckIn         => event.payload shouldBe CheckInPayload
          case other                     => fail(s"unexpected eventType: $other")
        }
      }

    "produce events with valid field values" in
      sampleEvents().foreach { event =>
        event.eventId should not be empty
        EventGenerator.UserIds should contain(event.userId)
        EventGenerator.BusinessIds should contain(event.businessId)
        event.timestampMs should be > 0L
      }

    "produce ReviewPayloads with a rating between 1 and 5" in
      sampleEvents()
        .collect { case ActivityEvent(_, _, _, _, _, p: ReviewPayload) => p }
        .foreach(_.rating should ((be >= 1).and(be <= 5)))

    "produce all four event types across a large sample" in {
      val types = sampleEvents().map(_.eventType).toSet
      types should contain(EventType.ReviewSubmitted)
      types should contain(EventType.PhotoUploaded)
      types should contain(EventType.BusinessViewed)
      types should contain(EventType.CheckIn)
    }

    "produce the same sequence for the same seed" in {
      val first = seededGenerator.stream().take(20).toList
      val second = seededGenerator.stream().take(20).toList
      first shouldBe second
    }
  }
}
