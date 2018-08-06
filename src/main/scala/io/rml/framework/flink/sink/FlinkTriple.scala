package io.rml.framework.flink.sink

import io.rml.framework.core.model._

/**
  * RDF Nodes for Flink that are serializable. These are used as output nodes.
  *
  * @param value
  */
abstract class FlinkRDFNode(val value: Entity) extends Serializable

abstract class FlinkRDFTermNode(val termNode: TermNode) extends FlinkRDFNode(termNode)

case class FlinkRDFBlank(blank: Blank) extends FlinkRDFTermNode(blank) {
  override def toString: String = {
    "_:" + blank.toString
  }
}

case class FlinkRDFResource(uri: Uri) extends FlinkRDFTermNode(uri) {

  override def toString: String = {
    val base = "<" + uri.toString + ">"
    base
  }
}

case class FlinkRDFLiteral(literal: Literal) extends FlinkRDFNode(literal) {
  override def toString: String = {
    val base = '"' + literal.toString + '"'
    if (literal.`type`.isDefined) {
      if (literal.language.isDefined) base + "^^<" + literal.`type`.get.toString + ">@" + literal.language.get.toString
      else base + "^^<" + literal.`type`.get.toString + ">"
    } else {
      if (literal.language.isDefined) base + "@" + literal.language.get.toString
      else base
    }
  }
}



case class FlinkRDFQuad(subject: FlinkRDFTermNode,
                        predicate: FlinkRDFResource,
                        `object`: FlinkRDFNode,
                        graph: Option[FlinkRDFResource] = None)
  extends  Serializable {

  override def toString: String = {
    s"$subject $predicate " + `object` + s" ${graph getOrElse ""}."

  }
}