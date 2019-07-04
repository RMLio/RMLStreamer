package io.rml.framework.flink.connector.kafka

sealed trait PartitionerFormat

case object FixedPartitioner extends PartitionerFormat
case object DefaultPartitioner extends  PartitionerFormat