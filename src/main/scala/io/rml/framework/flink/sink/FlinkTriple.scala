package io.rml.framework.flink.sink

import io.rml.framework.core.model.{Literal, Uri, Value}

abstract class FlinkRDFNode(val value: Value) extends Serializable

case class FlinkRDFResource(uri: Uri) extends FlinkRDFNode(uri) {

  override def toString: String = {
    val base = "<" + uri.toString + ">"
    base
  }
}

case class FlinkRDFLiteral(literal: Literal) extends FlinkRDFNode(literal) {
  override def toString: String = {
    val base = '"' + literal.toString + '"'
    if(literal.`type`.isDefined) {
      if(literal.language.isDefined) base + "^^<" + literal.`type`.get.toString + ">@" + literal.language.get.toString
      else  base + "^^<" + literal.`type`.get.toString + ">"
    } else {
      if(literal.language.isDefined) base + "@" + literal.language.get.toString
      else base
    }
  }
}

case class FlinkRDFTriple(subject: FlinkRDFResource, predicate: FlinkRDFResource, `object`: FlinkRDFNode) extends Serializable {
  override def toString: String = {
    subject + "  " + predicate + "  " + `object` + " ."
  }
}