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
