package com.andy327.kl.common.config

import java.util.concurrent.ExecutionException

import scala.jdk.CollectionConverters._

import org.apache.kafka.clients.admin.{AdminClient, NewTopic}
import org.apache.kafka.common.errors.TopicExistsException

/** Utility for creating Kafka topics via `AdminClient`.
  *
  * Topic creation is idempotent: topics that already exist are silently skipped. The caller is responsible for
  * constructing and closing the `AdminClient`.
  */
object TopicAdmin {

  /** Creates the specified topics, ignoring any that already exist.
    *
    * @param admin AdminClient used to issue the create requests
    * @param topics sequence of topic names to create
    * @param partitions number of partitions for each topic
    * @param replicationFactor replication factor for each topic
    */
  def createTopics(
      admin: AdminClient,
      topics: Seq[String],
      partitions: Int = 1,
      replicationFactor: Short = 1
  ): Unit = {
    val newTopics = topics.map(new NewTopic(_, partitions, replicationFactor)).asJava
    val results = admin.createTopics(newTopics)
    results.values().asScala.foreach { case (_, future) =>
      try future.get()
      catch {
        case e: ExecutionException if e.getCause.isInstanceOf[TopicExistsException] => ()
      }
    }
  }

  /** Creates all pipeline topics with the given partition and replication settings.
    *
    * @param admin AdminClient used to issue the create requests
    * @param partitions number of partitions for each topic
    * @param replicationFactor replication factor for each topic
    */
  def createAll(admin: AdminClient, partitions: Int = 1, replicationFactor: Short = 1): Unit =
    createTopics(admin, KafkaConfig.Topics.all, partitions, replicationFactor)
}
