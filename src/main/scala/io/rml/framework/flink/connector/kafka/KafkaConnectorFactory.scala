/**
  * MIT License
  *
  * Copyright (C) 2017 - 2020 RDF Mapping Language (RML)
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  *
  **/
package io.rml.framework.flink.connector.kafka

import java.lang
import java.nio.charset.StandardCharsets
import java.util.Properties
import io.rml.framework.core.internal.Logging
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.connectors.kafka._
import org.apache.kafka.clients.producer.{ProducerConfig, ProducerRecord}

import java.time.Duration

abstract class KafkaConnectorFactory {
  def getSource[T](topic: String, valueDeserializer: DeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T]

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
}

case object UniversalKafkaConnectorFactory extends KafkaConnectorFactory with Logging {

  override def getSource[T](topic: String, valueDeserializer: DeserializationSchema[T], props: Properties): FlinkKafkaConsumerBase[T] = {

    
    val consumer =  new FlinkKafkaConsumer[T](topic, valueDeserializer, props) 

    val strategy = WatermarkStrategy.forBoundedOutOfOrderness[T](Duration.ofMillis(50))
      .withIdleness(Duration.ofMillis(10))
    consumer.assignTimestampsAndWatermarks(strategy)

    consumer
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
