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
object PartitionerFormat {

  def fromString(string: String): PartitionerFormat = {
    string.toLowerCase() match{
      case "fixed" => FixedPartitioner
      case "default" => DefaultPartitioner
      case "kafka" => KafkaPartitioner
      case _ => throw new IllegalArgumentException(s"Partitioner format of type $string is not supported.")
    }
  }
}
case object FixedPartitioner extends PartitionerFormat
case object DefaultPartitioner extends  PartitionerFormat
case object KafkaPartitioner extends PartitionerFormat