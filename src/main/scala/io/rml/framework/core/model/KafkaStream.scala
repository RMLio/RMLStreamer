package io.rml.framework.core.model

import java.util.Properties

import io.rml.framework.core.vocabulary.RMLVoc
import io.rml.framework.flink.source.kafka.{KafkaConnectorFactory, KafkaConnectorVersionFactory}

case class KafkaStream(uri: Uri,
                       zookeepers: List[String],
                       brokers: List[String],
                       groupId: String,
                       topic: String,
                       version: String = RMLVoc.Property.KAFKA08) extends StreamDataSource {

  def getConnectorFactory: KafkaConnectorFactory = {
    KafkaConnectorVersionFactory(version)
  }

  def getProperties: Properties = {
    val properties = new Properties()
    val brokersCommaSeparated = brokers.reduce((a, b) => a + ", " + b)
    properties.setProperty("bootstrap.servers", brokersCommaSeparated)
    val zookeepersCommaSeparated = zookeepers.reduce((a, b) => a + ", " + b)
    properties.setProperty("zookeeper.connect", zookeepersCommaSeparated)
    properties.setProperty("group.id", groupId)
    properties.setProperty("auto.offset.reset", "earliest")
    properties
  }
}
