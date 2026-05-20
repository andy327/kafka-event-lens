package com.andy327.kl.common.model

/** An `ActivityEvent` that has passed validation and been enriched by the processor.
  *
  * @param eventId unique identifier for the originating event
  * @param userId ID of the user who performed the action
  * @param businessId ID of the business the event is associated with
  * @param eventType one of the constants defined in [[EventType]]
  * @param timestampMs client-side event time in epoch milliseconds
  * @param payload event-specific data; the concrete type corresponds to `eventType`
  * @param payloadType simple class name of the `payload` subtype (e.g. `"ReviewPayload"`)
  * @param processedAt wall-clock time in epoch milliseconds when the processor handled this event
  */
case class EnrichedEvent(
    eventId: String,
    userId: String,
    businessId: String,
    eventType: String,
    timestampMs: Long,
    payload: EventPayload,
    payloadType: String,
    processedAt: Long
)
