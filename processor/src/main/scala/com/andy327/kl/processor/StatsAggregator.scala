package com.andy327.kl.processor

import java.time.Duration

import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.kstream.{Materialized, TimeWindows}
import org.apache.kafka.streams.scala.kstream.{Grouped, KStream}
import org.apache.kafka.streams.scala.serialization.Serdes.stringSerde

import com.andy327.kl.common.model.ActivityEvent

/** Builds a windowed event-count aggregation per business and event type. */
object StatsAggregator {

  /** Name of the Kafka Streams state store backing the windowed counts. */
  val StoreName = "business-stats"

  /** Duration of each tumbling count window. */
  val WindowSize: Duration = Duration.ofMinutes(1)

  /** Attaches a windowed count aggregation to the given stream, grouped by business ID and event type.
    *
    * Results are materialized to the `business-stats` state store, whose changelog topic is created automatically by
    * Kafka Streams as `{applicationId}-business-stats-changelog`.
    *
    * @param stream valid activity event stream to aggregate
    * @param activitySerde Serde for `ActivityEvent`, required for the grouped stream
    */
  def buildStats(stream: KStream[String, ActivityEvent], activitySerde: Serde[ActivityEvent]): Unit = {
    implicit val grouped: Grouped[String, ActivityEvent] = Grouped.`with`(stringSerde, activitySerde)
    stream
      .groupBy((_, event) => s"${event.businessId}:${event.eventType}")
      .windowedBy(TimeWindows.ofSizeWithNoGrace(WindowSize))
      .count()(Materialized.as(StoreName))
    ()
  }
}
