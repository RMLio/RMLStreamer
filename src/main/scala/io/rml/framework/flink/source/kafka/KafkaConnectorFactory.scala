package io.rml.framework.flink.source.kafka

import java.util
import java.util.Properties

import io.rml.framework.core.vocabulary.RMLVoc
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.connectors.kafka._
import org.apache.flink.streaming.connectors.kafka.partitioner.{FlinkFixedPartitioner, FlinkKafkaPartitioner}
import org.apache.flink.streaming.util.serialization._

abstract class KafkaConnectorFactory {
  def getConsumer[T](topic: String, valueDeserializer: DeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]

  def getConsumer[T](topic: String, deserializer: KeyedDeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]

  def getConsumer[T](topics: util.List[String], deserializationSchema: DeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]

  def getConsumer[T](topics: util.List[String], deserializationSchema: KeyedDeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]


  def applyProducer[T](brokerList: String, topic: String, serializationSchema: SerializationSchema[T], dataStream: DataStream[T]): Unit = {
    applyProducer(brokerList, topic, new KeyedSerializationSchemaWrapper[T](serializationSchema), dataStream)

  }

  def applyProducer[T](brokerList: String, topic: String, serializationSchema: KeyedSerializationSchema[T], dataStream: DataStream[T]): Unit

}

object KafkaConnectorVersionFactory {

  def apply[T](version: String): Option[KafkaConnectorFactory] = {
    version match {
      case RMLVoc.Property.KAFKA08 => Some(KafkaConnector08Factory())
      case RMLVoc.Property.KAFKA09 => Some(KafkaConnector09Factory())
      case RMLVoc.Property.KAFKA010 => Some(KafkaConnector010Factory())
      case _ => None
    }
  }
}


case class KafkaConnector08Factory(version: String = RMLVoc.Property.KAFKA08) extends KafkaConnectorFactory {

  override def getConsumer[T](topic: String, valueDeserializer: DeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]
  = {
    new FlinkKafkaConsumer08[T](topic, valueDeserializer, props)
  }

  override def getConsumer[T](topic: String, deserializer: KeyedDeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]
  = {
    new FlinkKafkaConsumer08[T](topic, deserializer, props)


  }

  override def getConsumer[T](topics: util.List[String], deserializationSchema: DeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]
  = {
    new FlinkKafkaConsumer08[T](topics, deserializationSchema, props)
  }

  override def getConsumer[T](topics: util.List[String], deserializationSchema: KeyedDeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]
  = {
    new FlinkKafkaConsumer08[T](topics, deserializationSchema, props)
  }

  override def applyProducer[T](brokerList: String, topic: String, serializationSchema: KeyedSerializationSchema[T], dataStream: DataStream[T]): Unit = {
    val producer = new FlinkKafkaProducer08[T](brokerList, topic, serializationSchema)
    dataStream.addSink(producer)
  }
}

case class KafkaConnector09Factory(version: String = RMLVoc.Property.KAFKA09) extends KafkaConnectorFactory {

  override def getConsumer[T](topic: String, valueDeserializer: DeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]
  = {
    new FlinkKafkaConsumer09[T](topic, valueDeserializer, props)
  }

  override def getConsumer[T](topic: String, deserializer: KeyedDeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]
  = {
    new FlinkKafkaConsumer09[T](topic, deserializer, props)
  }

  override def getConsumer[T](topics: util.List[String], deserializationSchema: DeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]
  = {
    new FlinkKafkaConsumer09[T](topics, deserializationSchema, props)
  }

  override def getConsumer[T](topics: util.List[String], deserializationSchema: KeyedDeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T] = {
    new FlinkKafkaConsumer09[T](topics, deserializationSchema, props)
  }

  override def applyProducer[T](brokerList: String, topic: String, serializationSchema: KeyedSerializationSchema[T], dataStream: DataStream[T]): Unit = {
    val producer = new FlinkKafkaProducer09[T](brokerList, topic, serializationSchema)
    dataStream.addSink(producer)
  }
}


case class KafkaConnector010Factory(version: String = RMLVoc.Property.KAFKA010) extends KafkaConnectorFactory {
  override def getConsumer[T](topic: String, valueDeserializer: DeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]
  = {
    new FlinkKafkaConsumer010[T](topic, valueDeserializer, props)
  }

  override def getConsumer[T](topic: String, deserializer: KeyedDeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]
  = {
    new FlinkKafkaConsumer010[T](topic, deserializer, props)
  }

  override def getConsumer[T](topics: util.List[String], deserializationSchema: DeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]
  = {
    new FlinkKafkaConsumer010[T](topics, deserializationSchema, props)

  }

  override def getConsumer[T](topics: util.List[String], deserializationSchema: KeyedDeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]
  = {
    new FlinkKafkaConsumer010[T](topics, deserializationSchema, props)
  }

  override def applyProducer[T](brokerList: String, topic: String, serializationSchema: KeyedSerializationSchema[T], stream: DataStream[T]): Unit = {

    //TODO: fix this / update flink to latest version to use the stable api methods....
    /**
      * Trying to implement the same way as the code snippet from official website for flink 1.3 documentation
      * doesn't work due to method overloading error...
      *
      * Link: https://ci.apache.org/projects/flink/flink-docs-release-1.3/dev/connectors/kafka.html#kafka-producer
      *
      * The current implementation would'nt make use of latest kafka feature of attaching timestamps to the records of
      * triple.
      */
    val producer = new FlinkKafkaProducer010(brokerList, topic, serializationSchema)
    producer.setFlushOnCheckpoint(true)
    producer.setLogFailuresOnly(false)

    stream.addSink(producer)
  }
}