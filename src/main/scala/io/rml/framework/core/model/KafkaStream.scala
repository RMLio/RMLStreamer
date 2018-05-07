package io.rml.framework.core.model

class KafkaStream(val zookeepers: List[String],
                  val brokers: List[String],
                  val groupId: String,
                  val topic: String) {



}
