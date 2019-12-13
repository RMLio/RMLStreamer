package io.rml.framework.flink.connector.kafka

import java.nio.charset.StandardCharsets
import java.util.{Optional, Properties}
import java.{lang, util}

import io.rml.framework.core.internal.Logging
import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.connectors.kafka.partitioner.{FlinkFixedPartitioner, FlinkKafkaPartitioner}
import org.apache.flink.streaming.connectors.kafka._
import org.apache.flink.streaming.util.serialization.KeyedDeserializationSchema
import org.apache.kafka.clients.producer.{ProducerConfig, ProducerRecord}

abstract class KafkaConnectorFactory {
  def getSource[T](topic: String, valueDeserializer: DeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]

  def getSource[T](topic: String, deserializer: KeyedDeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]

  def getSource[T](topics: util.List[String], deserializationSchema: DeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]

  def getSource[T](topics: util.List[String], deserializationSchema: KeyedDeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]

  def applySink[T](bootstrapServers: String, rmlPartitionProperties: Properties, topic: String, dataStream: DataStream[T]): Unit

  def getProducerConfig(brokerList: String): Properties = {
    // set at-least-once delivery strategy. See https://ci.apache.org/projects/flink/flink-docs-stable/dev/connectors/kafka.html#kafka-09-and-010
    val producerConfig = FlinkKafkaProducerBase.getPropertiesFromBrokerList(brokerList)
    producerConfig.setProperty(ProducerConfig.RETRIES_CONFIG, "5")
    producerConfig.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "1")
    producerConfig
  }

  def generateCustomScheme[T](topic: String): KafkaSerializationSchema[T] = {
    generateCustomScheme(topic, Int.MaxValue)
  }

  def generateCustomScheme[T](topic: String , partition: Int): KafkaSerializationSchema[T] = {
    new KafkaSerializationSchema[T] {
      override def serialize(element: T, timestamp: lang.Long): ProducerRecord[Array[Byte], Array[Byte]] = {
        /*  if we ever want to serialise non-String objects:
        val stream: ByteArrayOutputStream = new ByteArrayOutputStream()
        val oos = new ObjectOutputStream(stream)
        oos.writeObject(element)
        oos.close() */
        val elementBytes = element.toString.getBytes(StandardCharsets.UTF_8) //stream.toByteArray
        if (partition == Int.MaxValue)
          new ProducerRecord[Array[Byte], Array[Byte]](topic, null, null, elementBytes)
        else
          new ProducerRecord[Array[Byte], Array[Byte]](topic, partition, null, elementBytes)
      }
    }
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

  override def applySink[T](bootstrapServers: String, rmlPartitionProperties: Properties, topic: String, dataStream: DataStream[T]): Unit = {
    logInfo(s"Connecting output to Kafka topic [$topic].")

    val kafkaProducerProperties = new Properties()
    kafkaProducerProperties.put("bootstrap.servers", bootstrapServers)

    val kafkaPartitionSchema: KafkaSerializationSchema[T] =
      if (rmlPartitionProperties.containsKey(RMLPartitioner.PARTITION_ID_PROPERTY) && !rmlPartitionProperties.get(RMLPartitioner.PARTITION_ID_PROPERTY).equals("__NO_VALUE_KEY")) {
        val partitionId = rmlPartitionProperties.get(RMLPartitioner.PARTITION_ID_PROPERTY).toString.toInt
        generateCustomScheme(topic, partitionId)
      } else {
        generateCustomScheme(topic)
      }

    val producer = new FlinkKafkaProducer(topic, kafkaPartitionSchema, kafkaProducerProperties, FlinkKafkaProducer.Semantic.AT_LEAST_ONCE)  // TODO: check: EXACTLY_ONCE doesn't work!
    producer.setLogFailuresOnly(false)
    dataStream.addSink(producer)
  }

}