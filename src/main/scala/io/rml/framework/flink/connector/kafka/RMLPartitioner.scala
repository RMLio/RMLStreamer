package io.rml.framework.flink.connector.kafka

import java.util.Properties

import org.apache.flink.streaming.connectors.kafka.partitioner.FlinkKafkaPartitioner

/**
  *
  * Abstract custom RML partitioner which will be used to partition the output
  * @param properties Properties containing extra information needed by the custom partitioner
  * @tparam T type of record
  */
abstract class RMLPartitioner[T](properties:Properties) extends FlinkKafkaPartitioner[T]



/**
  * The partitioner will send all the records to a partition with id specified by the user.
  */
class RMLFixedPartitioner[T](properties: Properties) extends RMLPartitioner[T](properties){


  private val partitionID =  properties.getProperty(RMLPartitioner.PARTITION_ID_PROPERTY).toInt
  /**
    * Send the record to a particular partition.
    *
    * @param record  string record to be partitioned
    * @param key     key of the record in bytes array
    * @param value   value of the record in bytes array
    * @param targetTopic output kafka topic
    * @param partitions  array of partitions
    * @return index of the partition to which the record will be sent
    */
  override def partition(record: T, key: Array[Byte], value: Array[Byte], targetTopic: String, partitions: Array[Int]): Int = {
    if (partitions == null && partitions.length <= 0) {
      throw new IllegalArgumentException("Partitions of the target topic is empty.")
    }

    partitionID % partitions.length
  }
}

object RMLPartitioner {
  val PARTITION_ID_PROPERTY =  "partition-id"
  val PARTITION_FORMAT_PROPERTY = "partition-format"
}
