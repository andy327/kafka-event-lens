package com.andy327.kl.processor

import com.andy327.kl.common.model.{ActivityEvent, EnrichedEvent}

/** Enriches a validated `ActivityEvent` with derived fields for downstream consumers. */
object EventEnricher {

  /** Produces an `EnrichedEvent` from a validated `ActivityEvent`.
    *
    * Adds `payloadType` (the simple class name of the payload subtype) and `processedAt` (the wall-clock time at the
    * moment of enrichment in epoch milliseconds).
    *
    * @param event the validated event to enrich
    */
  def enrich(event: ActivityEvent): EnrichedEvent =
    EnrichedEvent(
      eventId = event.eventId,
      userId = event.userId,
      businessId = event.businessId,
      eventType = event.eventType,
      timestampMs = event.timestampMs,
      payload = event.payload,
      payloadType = event.payload.getClass.getSimpleName,
      processedAt = System.currentTimeMillis()
    )
}
