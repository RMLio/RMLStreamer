package io.rml.framework.flink.connector.kafka

import java.util
import java.util.Properties

import io.rml.framework.core.model.{Kafka010, Kafka09, KafkaVersion}
import org.apache.flink.streaming.connectors.kafka.internals.KeyedSerializationSchemaWrapper
import org.apache.kafka.clients.producer.ProducerConfig
//import io.rml.framework.core.vocabulary.RMLVoc
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.connectors.kafka._
import org.apache.flink.streaming.util.serialization._

abstract class KafkaConnectorFactory {
  def getSource[T](topic: String, valueDeserializer: DeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]

  def getSource[T](topic: String, deserializer: KeyedDeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]

  def getSource[T](topics: util.List[String], deserializationSchema: DeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]

  def getSource[T](topics: util.List[String], deserializationSchema: KeyedDeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]


  def applySink[T](brokerList: String, topic: String, serializationSchema: SerializationSchema[T], dataStream: DataStream[T]): Unit = {
    applySink(brokerList, topic, new KeyedSerializationSchemaWrapper[T](serializationSchema), dataStream)

  }

  def applySink[T](brokerList: String, topic: String, serializationSchema: KeyedSerializationSchema[T], dataStream: DataStream[T]): Unit

  def getProducerConfig(brokerList: String): Properties = {
    // set at-least-once delivery strategy. See https://ci.apache.org/projects/flink/flink-docs-stable/dev/connectors/kafka.html#kafka-09-and-010
    val producerConfig = FlinkKafkaProducerBase.getPropertiesFromBrokerList(brokerList);
    producerConfig.setProperty(ProducerConfig.RETRIES_CONFIG, "5");
    producerConfig.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "1");
    producerConfig
  }

}

object KafkaConnectorVersionFactory {

  def apply[T](version: KafkaVersion): Option[KafkaConnectorFactory] = {

    version match {
      //case Kafka08 => Some(KafkaConnector08Factory)
      case Kafka09 => Some(KafkaConnector09Factory)
      case Kafka010 => Some(KafkaConnector010Factory)
      case _ => None
    }
  }
}


//case object KafkaConnector08Factory extends KafkaConnectorFactory {
//
//  override def getSource[T](topic: String, valueDeserializer: DeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]
//  = {
//    new FlinkKafkaConsumer08[T](topic, valueDeserializer, props)
//  }
//
//  override def getSource[T](topic: String, deserializer: KeyedDeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]
//  = {
//    new FlinkKafkaConsumer08[T](topic, deserializer, props)
//
//
//  }
//
//  override def getSource[T](topics: util.List[String], deserializationSchema: DeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]
//  = {
//    new FlinkKafkaConsumer08[T](topics, deserializationSchema, props)
//  }
//
//  override def getSource[T](topics: util.List[String], deserializationSchema: KeyedDeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]
//  = {
//    new FlinkKafkaConsumer08[T](topics, deserializationSchema, props)
//  }
//
//  override def applySink[T](brokerList: String, topic: String, serializationSchema: KeyedSerializationSchema[T], dataStream: DataStream[T]): Unit = {
//    val producer = new FlinkKafkaProducer08[T](brokerList, topic, serializationSchema)
//    producer.setFlushOnCheckpoint(true)
//    producer.setLogFailuresOnly(false)
//
//    dataStream.addSink(producer)
//  }
//}

case object KafkaConnector09Factory extends KafkaConnectorFactory {

  override def getSource[T](topic: String, valueDeserializer: DeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]
  = {
    new FlinkKafkaConsumer09[T](topic, valueDeserializer, props)
  }

  override def getSource[T](topic: String, deserializer: KeyedDeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]
  = {
    new FlinkKafkaConsumer09[T](topic, deserializer, props)
  }

  override def getSource[T](topics: util.List[String], deserializationSchema: DeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]
  = {
    new FlinkKafkaConsumer09[T](topics, deserializationSchema, props)
  }

  override def getSource[T](topics: util.List[String], deserializationSchema: KeyedDeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T] = {
    new FlinkKafkaConsumer09[T](topics, deserializationSchema, props)
  }

  override def applySink[T](brokerList: String, topic: String, serializationSchema: KeyedSerializationSchema[T], dataStream: DataStream[T]): Unit = {
    val producer = new FlinkKafkaProducer09[T](topic, serializationSchema, getProducerConfig(brokerList));
    producer.setFlushOnCheckpoint(true)
    producer.setLogFailuresOnly(false)
    dataStream.addSink(producer)
  }
}


case object KafkaConnector010Factory extends KafkaConnectorFactory {
  override def getSource[T](topic: String, valueDeserializer: DeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]
  = {
    new FlinkKafkaConsumer010[T](topic, valueDeserializer, props)
  }

  override def getSource[T](topic: String, deserializer: KeyedDeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]
  = {
    new FlinkKafkaConsumer010[T](topic, deserializer, props)
  }

  override def getSource[T](topics: util.List[String], deserializationSchema: DeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]
  = {
    new FlinkKafkaConsumer010[T](topics, deserializationSchema, props)

  }

  override def getSource[T](topics: util.List[String], deserializationSchema: KeyedDeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]
  = {
    new FlinkKafkaConsumer010[T](topics, deserializationSchema, props)
  }

  override def applySink[T](brokerList: String, topic: String, serializationSchema: KeyedSerializationSchema[T], dataStream: DataStream[T]): Unit = {

    //TODO: fix this / update flink to latest version to use the stable api methods....
    /**
      * Trying to implement the same way as the code snippet from official website for flink 1.3 documentation
      * doesn't work due to method overloading error...
      *
      * Link: https://ci.apache.org/projects/flink/flink-docs-release-1.3/dev/connectors/kafka.html#kafka-producer
      *
      * The current implementation wouldn't make use of latest kafka feature of attaching timestamps to the records of
      * triple.
      */
    val producer = new FlinkKafkaProducer010[T](topic, serializationSchema, getProducerConfig(brokerList));
    producer.setFlushOnCheckpoint(true)
    producer.setLogFailuresOnly(false)
    dataStream.addSink(producer)
  }

}