package com.andy327.kl.common.model

/** Event-specific data carried inside an [[ActivityEvent]] envelope. */
sealed trait EventPayload

/** Payload for a user-submitted business review.
  *
  * @param rating integer rating from 1 to 5
  * @param reviewText free-text content of the review
  */
case class ReviewPayload(
    rating: Int,
    reviewText: String
) extends EventPayload

/** Payload for a user-uploaded photo of a business.
  *
  * @param photoUrl URL of the uploaded photo
  */
case class PhotoPayload(
    photoUrl: String
) extends EventPayload

/** Payload for a user viewing a business page.
  *
  * @param referrer how the user arrived at the page (e.g. "search", "direct")
  * @param durationMs time spent on the page in milliseconds; absent if the session ended uncleanly
  */
case class ViewPayload(
    referrer: Option[String],
    durationMs: Option[Long]
) extends EventPayload

/** Payload for a user checking in at a business. */
case object CheckInPayload extends EventPayload

/** String constants for the `eventType` field of [[ActivityEvent]]. */
object EventType {
  val ReviewSubmitted = "REVIEW_SUBMITTED"
  val PhotoUploaded = "PHOTO_UPLOADED"
  val BusinessViewed = "BUSINESS_VIEWED"
  val CheckIn = "CHECK_IN"

  val all: Set[String] = Set(ReviewSubmitted, PhotoUploaded, BusinessViewed, CheckIn)
}

/** Envelope for all user activity events published to the raw topic.
  *
  * Keyed by `userId` in Kafka so all events from a given user land on the same partition, preserving per-user ordering.
  *
  * @param eventId unique identifier for this event
  * @param userId ID of the user who performed the action; also the Kafka partition key
  * @param businessId ID of the business the event is associated with
  * @param eventType one of the constants defined in [[EventType]]
  * @param timestampMs client-side event time in epoch milliseconds
  * @param payload event-specific data; the concrete type corresponds to `eventType`
  */
case class ActivityEvent(
    eventId: String,
    userId: String,
    businessId: String,
    eventType: String,
    timestampMs: Long,
    payload: EventPayload
)
