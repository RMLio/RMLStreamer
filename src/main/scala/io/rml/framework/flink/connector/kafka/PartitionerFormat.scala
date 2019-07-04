package io.rml.framework.flink.connector.kafka


/**
  * Scala enum implementation for determining partitioning format.
  *
  * FixedPartitioner will set the output partition to a specific id given by the user as argument.
  *
  * DefaultPartitioner will use the default partitioning scheme by Flink.
  *
  */
sealed trait PartitionerFormat

case object FixedPartitioner extends PartitionerFormat
case object DefaultPartitioner extends  PartitionerFormat