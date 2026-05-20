package com.andy327.kl.processor

import com.andy327.kl.common.model.{ActivityEvent, EventType}

/** Validates incoming `ActivityEvent`s before they enter the enrichment pipeline. */
object EventValidator {

  /** Returns true if the event passes all validity checks.
    *
    * An event is valid if all required string fields are non-empty, `timestampMs` is positive, and `eventType` is one
    * of the four known constants in `EventType`.
    *
    * @param event the event to validate
    */
  def isValid(event: ActivityEvent): Boolean =
    event.eventId.nonEmpty &&
    event.userId.nonEmpty &&
    event.businessId.nonEmpty &&
    event.timestampMs > 0L &&
    EventType.all.contains(event.eventType)
}
