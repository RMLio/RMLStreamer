package io.rml.framework.flink.connector.kafka

import org.apache.flink.streaming.connectors.kafka.partitioner.FlinkKafkaPartitioner

class RMLFixedPartitioner(partitionID : Int) extends FlinkKafkaPartitioner[String]{
  override def partition(record: String, key: Array[Byte], value: Array[Byte], targetTopic: String, partitions: Array[Int]): Int = {
    if (partitions == null && partitions.length <= 0) {
      throw new IllegalArgumentException("Partitions of the target topic is empty.")
    }

    partitionID % partitions.length
  }
}
