package io.rml.framework.core.model

case class KafkaStream(uri: Uri,
                       zookeepers: List[String],
                       brokers: List[String],
                       groupId: String,
                       topic: String,
                       version: String = "") extends StreamDataSource
