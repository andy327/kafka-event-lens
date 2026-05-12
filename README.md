# kafka-event-lens

[![CI](https://github.com/andy327/kafka-event-lens/actions/workflows/ci.yml/badge.svg)](https://github.com/andy327/kafka-event-lens/actions/workflows/ci.yml)
[![Scaladocs](https://github.com/andy327/kafka-event-lens/actions/workflows/scaladocs.yml/badge.svg)](https://github.com/andy327/kafka-event-lens/actions/workflows/scaladocs.yml)
[![codecov](https://codecov.io/gh/andy327/kafka-event-lens/branch/main/graph/badge.svg)](https://codecov.io/gh/andy327/kafka-event-lens)
[![Scala](https://img.shields.io/badge/Scala-2.13-red?logo=scala&logoColor=red)](https://www.scala-lang.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

A Kafka event streaming pipeline built in Scala, modelling a Yelp-like user activity stream. Synthetic events
representing user actions on a local business platform flow through a producer, a Kafka Streams processor, and a
consumer sink — with Avro serialization, a Schema Registry, a dead-letter queue, and consumer lag reporting throughout.

## Project structure

```
common/     — shared domain model, Avro SerDes, Kafka config, topic admin
producer/   — synthetic event generator and Kafka producer
processor/  — Kafka Streams topology: enrichment, windowed aggregation, DLQ routing
consumer/   — ActivitySink (manual offset commit) and LagReporter
```

## Running locally

Start the Kafka broker and Schema Registry:

```bash
docker compose -f docker/docker-compose.yml up -d
```

Then run each component from sbt:

```bash
sbt "producer/run"
sbt "processor/run"
sbt "consumer/run"
```
