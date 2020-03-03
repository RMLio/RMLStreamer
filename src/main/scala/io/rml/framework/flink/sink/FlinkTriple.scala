/**
  * MIT License
  *
  * Copyright (C) 2017 - 2020 RDF Mapping Language (RML)
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  *
  **/
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
