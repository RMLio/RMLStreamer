package io.rml.framework.flink

import io.rml.framework.core.model.{Literal, Uri, Value}

abstract class FlinkRDFNode(value: Value) extends Serializable

case class FlinkRDFResource(uri: Uri) extends FlinkRDFNode(uri) {
  override def toString: String = "<" + uri.toString + ">"
}

case class FlinkRDFLiteral(literal: Literal) extends FlinkRDFNode(literal) {
  override def toString: String = '"' + literal.toString + '"'
}

case class FlinkRDFTriple(subject: FlinkRDFResource, predicate: FlinkRDFResource, `object`: FlinkRDFNode) extends Serializable {
  override def toString: String = {
    subject + "  " + predicate + "  " + `object` + " ."
  }
}