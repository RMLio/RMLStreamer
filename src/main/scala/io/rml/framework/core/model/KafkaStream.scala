package io.rml.framework.core.model

import java.util.{Objects, Properties}

import io.rml.framework.flink.connector.kafka.{KafkaConnectorFactory, KafkaConnectorVersionFactory}
import io.rml.framework.shared.RMLException

case class KafkaStream(
                       brokers: List[String],
                       groupId: String,
                       topic: String/*,
                       version: KafkaVersion*/) extends StreamDataSource {
  def getConnectorFactory: KafkaConnectorFactory = {
    KafkaConnectorVersionFactory(/*version*/).get
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

    val totalHash = Objects.hash(groupId, topic, /*version,*/ brokers.reduce((a,b)=> a + "," + b))

    Uri(totalHash.toHexString)
  }
}


sealed abstract  class KafkaVersion(val version: String) extends Serializable
//case object Kafka09 extends KafkaVersion("0.9.x")
//case object Kafka010 extends KafkaVersion("0.10.x")
case object Kafka extends KafkaVersion("universal")

object KafkaVersion{
  def apply(ver:String):KafkaVersion = ver match {

    //case Kafka09.version => Kafka09
    //case Kafka010.version => Kafka010
    case Kafka.version => Kafka
    case _ =>
      throw new RMLException(s"Current RML streamer doesn't support kafka version: $ver \n"
        + s"Supported versions are ${KafkaVersion.SUPPORTED_VERSIONS.map(_.version).reduceLeft((a,b)=> s"$a, $b")}" )

  }

  val SUPPORTED_VERSIONS = Set(/*Kafka09, Kafka010,*/ Kafka)
}

