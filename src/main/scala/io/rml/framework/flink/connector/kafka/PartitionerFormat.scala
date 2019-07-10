package io.rml.framework.flink.connector.kafka


/**
  * Scala enum implementation for determining partitioning format.
  *
  * FixedPartitioner will set the output partition to a specific id given by the user as argument.
  *
  * DefaultPartitioner will use the default partitioning scheme by Flink.
  *
  */
sealed trait PartitionerFormat {
  def string():String
}
object PartitionerFormat {

  def fromString(string: String): PartitionerFormat = {
    string.toLowerCase() match{
      case "fixed" => FixedPartitioner
      case "kafka" => KafkaPartitioner
      case _ => DefaultPartitioner
    }
  }
}
case object FixedPartitioner extends PartitionerFormat{
  def string():String ={
    "fixed"
  }
}
case object DefaultPartitioner extends  PartitionerFormat {
  override def string(): String = {
    "default"
  }
}
case object KafkaPartitioner extends PartitionerFormat {
  override def string(): String = {
    "kafka"
  }
}