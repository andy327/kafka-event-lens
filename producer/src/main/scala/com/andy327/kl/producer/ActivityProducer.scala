package com.andy327.kl.producer

import java.io.Closeable

import org.slf4j.LoggerFactory

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import com.andy327.kl.common.config.KafkaConfig
import com.andy327.kl.common.model.ActivityEvent

/** Publishes `ActivityEvent`s to the raw activity topic using an async send with per-record callback logging.
  *
  * The caller is responsible for constructing and closing the `KafkaProducer`.
  *
  * @param producer configured Kafka producer for sending `ActivityEvent` records
  */
class ActivityProducer(producer: KafkaProducer[String, ActivityEvent]) extends Closeable {

  private val logger = LoggerFactory.getLogger(getClass)

  /** Sends each event from the stream to Kafka, blocking until the stream is exhausted or the thread is interrupted.
    *
    * @param events stream of `ActivityEvent`s to publish
    */
  def run(events: LazyList[ActivityEvent]): Unit = events.foreach(send)

  /** Sends a single `ActivityEvent` to the raw activity topic, keyed by user ID.
    *
    * The send is asynchronous; delivery success or failure is logged via callback on the producer I/O thread.
    *
    * @param event the event to publish
    */
  def send(event: ActivityEvent): Unit = {
    val record = new ProducerRecord[String, ActivityEvent](KafkaConfig.Topics.UserActivityRaw, event.userId, event)
    producer.send(
      record,
      (metadata, exception) =>
        if (exception != null)
          logger.error(s"Failed to send eventId=${event.eventId} userId=${event.userId}: ${exception.getMessage}")
        else
          logger.debug(
            s"Sent eventId=${event.eventId} to ${metadata.topic()} partition=${metadata.partition()} offset=${metadata.offset()}"
          )
    )
  }

  override def close(): Unit = producer.close()
}
