package io.rml.framework.core.model

import java.util.Properties

import io.rml.framework.core.vocabulary.RMLVoc
import io.rml.framework.flink.connector.kafka.{KafkaConnectorFactory, KafkaConnectorVersionFactory}
import io.rml.framework.shared.RMLException

case class KafkaStream(uri: Uri,
                       zookeepers: List[String],
                       brokers: List[String],
                       groupId: String,
                       topic: String,
                       version: KafkaVersion = Kafka010) extends StreamDataSource {
  def getConnectorFactory: KafkaConnectorFactory = {
    KafkaConnectorVersionFactory(version).get
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


sealed abstract  class KafkaVersion(val version: String) extends Serializable
case object Kafka08 extends KafkaVersion("0.8.x")
case object Kafka09 extends KafkaVersion("0.9.x")
case object Kafka010 extends KafkaVersion("0.10.x")

object KafkaVersion{
  def apply(ver:String):KafkaVersion = ver match {

    case Kafka08.version => Kafka08
    case Kafka09.version => Kafka09
    case Kafka010.version => Kafka010
    case _ =>
      throw new RMLException(s"Current RML streamer doesn't support kafka version: $ver \n"
        + s"Supported versions are ${KafkaVersion.SUPPORTED_VERSIONS.map(_.version).reduceLeft((a,b)=> s"$a, $b")}" )

  }

  val SUPPORTED_VERSIONS = Set(Kafka08, Kafka09, Kafka010)
}

