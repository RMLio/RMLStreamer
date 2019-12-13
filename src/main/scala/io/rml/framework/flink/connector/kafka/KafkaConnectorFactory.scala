package io.rml.framework.flink.connector.kafka

import java.util
import java.util.{Optional, Properties}

import io.rml.framework.core.internal.Logging
import org.apache.flink.api.common.serialization.{DeserializationSchema, SerializationSchema}
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.connectors.kafka._
import org.apache.flink.streaming.connectors.kafka.internals.KeyedSerializationSchemaWrapper
import org.apache.flink.streaming.connectors.kafka.partitioner.{FlinkFixedPartitioner, FlinkKafkaPartitioner}
import org.apache.flink.streaming.util.serialization.{KeyedDeserializationSchema, KeyedSerializationSchema}
import org.apache.kafka.clients.producer.ProducerConfig

abstract class KafkaConnectorFactory {
  def getSource[T](topic: String, valueDeserializer: DeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]

  def getSource[T](topic: String, deserializer: KeyedDeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]

  def getSource[T](topics: util.List[String], deserializationSchema: DeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]

  def getSource[T](topics: util.List[String], deserializationSchema: KeyedDeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]


  def applySink[T](brokerList: String, topic: String, partitionerProperty: Properties, serializationSchema: SerializationSchema[T], dataStream: DataStream[T]): Unit = {

    val properties = getProducerConfig(brokerList)

    applySink(properties, topic, new KeyedSerializationSchemaWrapper[T](serializationSchema), dataStream, generatePartitioner[T](partitionerProperty))

  }

  def applySink[T](properties: Properties, topic: String, serializationSchema: KeyedSerializationSchema[T], dataStream: DataStream[T], partitioner: Optional[FlinkKafkaPartitioner[T]]): Unit

  def getProducerConfig(brokerList: String): Properties = {
    // set at-least-once delivery strategy. See https://ci.apache.org/projects/flink/flink-docs-stable/dev/connectors/kafka.html#kafka-09-and-010
    val producerConfig = FlinkKafkaProducerBase.getPropertiesFromBrokerList(brokerList)
    producerConfig.setProperty(ProducerConfig.RETRIES_CONFIG, "5")
    producerConfig.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "1")
    producerConfig
  }


  def generatePartitioner[T](properties: Properties): Optional[FlinkKafkaPartitioner[T]] = {

    val formatString =  properties.getProperty(RMLPartitioner.PARTITION_FORMAT_PROPERTY)
    val format = PartitionerFormat.fromString(formatString)
    format match {
      case FixedPartitioner => Optional.of(new RMLFixedPartitioner(properties))
      case KafkaPartitioner => Optional.ofNullable(null)
      case _ => Optional.of(new FlinkFixedPartitioner[T]())
    }
  }

}

case object UniversalKafkaConnectorFactory extends KafkaConnectorFactory with Logging {

  override def getSource[T](topic: String, valueDeserializer: DeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T] = {
    new FlinkKafkaConsumer[T](topic, valueDeserializer, props)
  }

  override def getSource[T](topic: String, deserializer: KeyedDeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T] = {
    new FlinkKafkaConsumer[T](topic, deserializer, props)
  }

  override def getSource[T](topics: util.List[String], deserializationSchema: DeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T] = {
    new FlinkKafkaConsumer[T](topics, deserializationSchema, props)
  }

  override def getSource[T](topics: util.List[String], deserializationSchema: KeyedDeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T] = {
    new FlinkKafkaConsumer[T](topics, deserializationSchema, props)
  }

  override def applySink[T](properties: Properties, topic: String, serializationSchema: KeyedSerializationSchema[T], dataStream: DataStream[T], partitioner: Optional[FlinkKafkaPartitioner[T]]): Unit = {
    logInfo("Connecting output to Kafka topic [" + topic + "]. SerializationSchema: [" + serializationSchema.getClass.getName + "]")
    if (partitioner.isPresent) {
      logInfo("Partitioner: [" + partitioner.get().getClass.getName + "].")
    }
    val producer = new FlinkKafkaProducer[T](topic, serializationSchema, properties, partitioner)
    //producer.setFlushOnCheckpoint(true)
    producer.setLogFailuresOnly(false)
    dataStream.addSink(producer)
  }

}