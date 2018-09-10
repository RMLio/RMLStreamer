package io.rml.framework.flink.source.kafka

import java.util
import java.util.Properties

import io.rml.framework.core.vocabulary.RMLVoc
import io.rml.framework.shared.RMLException
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.connectors.kafka.partitioner.{FlinkFixedPartitioner, FlinkKafkaPartitioner}
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaProducer010, _}
import org.apache.flink.streaming.util.serialization.{DeserializationSchema, KeyedDeserializationSchema, KeyedSerializationSchema, SerializationSchema}

trait KafkaConnectorFactory {
  def getConsumer[T](topic: String, valueDeserializer: DeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]

  def getConsumer[T](topic: String, deserializer: KeyedDeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]

  def getConsumer[T](topics: util.List[String], deserializationSchema: DeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]

  def getConsumer[T](topics: util.List[String], deserializationSchema: KeyedDeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]


  def getOrApplyProducer[T](brokerList: String, topic: String, serializationSchema: SerializationSchema[T], dataStream: Option[DataStream[T]]): Either[FlinkKafkaProducerBase[T], Unit]

  def getOrApplyProducer[T](brokerList: String, topic: String, serializationSchema: KeyedSerializationSchema[T], dataStream: Option[DataStream[T]]): Either[FlinkKafkaProducerBase[T], Unit]

}

object KafkaConnectorVersionFactory {

  def apply[T](version: String, dataStream: DataStream[T]): KafkaConnectorFactory = {
    version match {
      case RMLVoc.Property.KAFKA08 => KafkaConnector08Factory()
      case RMLVoc.Property.KAFKA09 => KafkaConnector09Factory()
      case RMLVoc.Property.KAFKA010 => KafkaConnector010Factory()
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

  override def getOrApplyProducer[T](brokerList: String, topic: String, serializationSchema: SerializationSchema[T], dataStream: Option[DataStream[T]]): Either[FlinkKafkaProducerBase[T], Unit] = {
    Left(new FlinkKafkaProducer08[T](brokerList,topic,serializationSchema))
  }

  override def getOrApplyProducer[T](brokerList: String, topic: String, serializationSchema: KeyedSerializationSchema[T], dataStream: Option[DataStream[T]]): Either[FlinkKafkaProducerBase[T], Unit] = {
    Left(new FlinkKafkaProducer08[T](brokerList, topic, serializationSchema)) 
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

  override def getOrApplyProducer[T](brokerList: String, topic: String, serializationSchema: SerializationSchema[T], dataStream: Option[DataStream[T]]): Either[FlinkKafkaProducerBase[T], Unit] = {
    Left(new FlinkKafkaProducer09[T](brokerList, topic, serializationSchema))
  }

  override def getOrApplyProducer[T](brokerList: String, topic: String, serializationSchema: KeyedSerializationSchema[T], dataStream: Option[DataStream[T]]): Either[FlinkKafkaProducerBase[T], Unit] = {
    Left(new FlinkKafkaProducer09[T](brokerList, topic, serializationSchema))
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

  override def getOrApplyProducer[T](brokerList: String, topic: String, serializationSchema: SerializationSchema[T], dataStream: Option[DataStream[T]]): Either[FlinkKafkaProducerBase[T], Unit] = {
    if(dataStream.isEmpty){
      throw new RMLException("Data stream must be provided to the method getOrApplyProducer for instantiating FlinkKafkaProducer010")
    }
    val stream = dataStream.get

    FlinkKafkaProducer010.writeToKafkaWithTimestamps[T](stream,topic, serializationSchema, FlinkKafkaProducerBase.getPropertiesFromBrokerList(brokerList))
    Right()
  }

  override def getOrApplyProducer[T](brokerList: String, topic: String, serializationSchema: KeyedSerializationSchema[T], dataStream: Option[DataStream[T]]): Either[FlinkKafkaProducerBase[T], Unit] = {
    if(dataStream.isEmpty){
      throw new RMLException("Data stream must be provided to the method getOrApplyProducer for instantiating FlinkKafkaProducer010")
    }
    val stream = dataStream.get

    FlinkKafkaProducer010.writeToKafkaWithTimestamps[T](stream,topic, serializationSchema, FlinkKafkaProducerBase.getPropertiesFromBrokerList(brokerList))
    Right()
  }
}