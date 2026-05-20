package com.andy327.kl.producer

import java.util.UUID

import scala.util.Random

import com.andy327.kl.common.model._

/** Generates a continuous stream of synthetic `ActivityEvent`s for use by the Kafka producer.
  *
  * @param random source of randomness; inject a seeded instance for reproducible output
  * @param clock source of the current time in epoch milliseconds; inject a fixed value for reproducible output
  */
class EventGenerator(random: Random = new Random(), clock: () => Long = System.currentTimeMillis) {

  /** An infinite lazy stream of synthetic `ActivityEvent`s. */
  def stream(): LazyList[ActivityEvent] = LazyList.continually(nextEvent())

  private def nextEvent(): ActivityEvent = {
    val userId = EventGenerator.UserIds(random.nextInt(EventGenerator.UserIds.size))
    val businessId = EventGenerator.BusinessIds(random.nextInt(EventGenerator.BusinessIds.size))
    val eventType = EventGenerator.EventTypes(random.nextInt(EventGenerator.EventTypes.size))
    val payload = eventType match {
      case EventType.ReviewSubmitted =>
        val text = EventGenerator.ReviewTexts(random.nextInt(EventGenerator.ReviewTexts.size))
        ReviewPayload(rating = random.nextInt(5) + 1, reviewText = text)
      case EventType.PhotoUploaded =>
        PhotoPayload(photoUrl =
          s"https://photos.example.com/$businessId/${new UUID(random.nextLong(), random.nextLong())}.jpg"
        )
      case EventType.BusinessViewed =>
        val referrer =
          if (random.nextBoolean()) Some(EventGenerator.Referrers(random.nextInt(EventGenerator.Referrers.size)))
          else None
        val duration = if (random.nextBoolean()) Some(random.nextInt(300000).toLong + 1000L) else None
        ViewPayload(referrer = referrer, durationMs = duration)
      case _ =>
        CheckInPayload
    }
    ActivityEvent(
      eventId = new UUID(random.nextLong(), random.nextLong()).toString,
      userId = userId,
      businessId = businessId,
      eventType = eventType,
      timestampMs = clock(),
      payload = payload
    )
  }
}

/** Constants used by [[EventGenerator]] to produce synthetic event data. */
object EventGenerator {

  /** Pool of synthetic user IDs sampled when generating events. */
  val UserIds: Seq[String] = (1 to 10).map(i => f"user-$i%03d")

  /** Pool of synthetic business IDs sampled when generating events. */
  val BusinessIds: Seq[String] = (1 to 10).map(i => f"business-$i%03d")

  /** All event type strings, sampled uniformly when generating events. */
  val EventTypes: Seq[String] = EventType.all.toSeq

  /** Pool of sample review texts used in `ReviewPayload`s. */
  val ReviewTexts: Seq[String] = Seq(
    "Great place, will definitely come back!",
    "Service was a bit slow but the food made up for it.",
    "Absolutely loved the atmosphere.",
    "Decent experience, nothing special.",
    "Would not recommend — very disappointed."
  )

  /** Pool of referrer labels used in `ViewPayload`s. */
  val Referrers: Seq[String] = Seq("search", "direct", "recommendation", "social")
}
