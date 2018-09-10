package io.rml.framework.core.model

import io.rml.framework.core.vocabulary.RMLVoc

case class KafkaStream(uri: Uri,
                       zookeepers: List[String],
                       brokers: List[String],
                       groupId: String,
                       topic: String,
                       version: String = RMLVoc.Property.KAFKA08) extends StreamDataSource
