package io.rml.framework.core.model

import java.util.{Objects, Properties}

import io.rml.framework.flink.connector.kafka.{KafkaConnectorFactory, UniversalKafkaConnectorFactory}

case class KafkaStream(
                       brokers: List[String],
                       groupId: String,
                       topic: String) extends StreamDataSource {
  def getConnectorFactory: KafkaConnectorFactory = {
   UniversalKafkaConnectorFactory
  }

  def getProperties: Properties = {
    val properties = new Properties()
    val brokersCommaSeparated = brokers.reduce((a, b) => a + ", " + b)
    properties.setProperty("bootstrap.servers", brokersCommaSeparated)
    properties.setProperty("group.id", groupId)
    properties.setProperty("auto.offset.reset", "earliest")
    properties
  }

  override def uri: ExplicitNode = {

    val totalHash = Objects.hash(groupId, topic, brokers.reduce((a,b)=> a + "," + b))

    Uri(totalHash.toHexString)
  }
}
