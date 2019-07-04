package io.rml.framework.flink.connector.kafka

import org.apache.flink.streaming.connectors.kafka.partitioner.FlinkKafkaPartitioner

/**
  * The partitioner will send all the records to a partition with id specified by the user.
  * @param partitionID partition id specified by the user
  */
class RMLFixedPartitioner(partitionID : Int) extends FlinkKafkaPartitioner[String]{

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
  override def partition(record: String, key: Array[Byte], value: Array[Byte], targetTopic: String, partitions: Array[Int]): Int = {
    if (partitions == null && partitions.length <= 0) {
      throw new IllegalArgumentException("Partitions of the target topic is empty.")
    }

    partitionID % partitions.length
  }
}
