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
package io.rml.framework.core.extractors.std

import io.rml.framework.core.extractors.{FunctionMapExtractor, GraphMapExtractor}
import io.rml.framework.core.model.rdf.{RDFNode, RDFResource}
import io.rml.framework.core.model.{GraphMap, Literal, Uri}
import io.rml.framework.core.vocabulary.R2RMLVoc
import io.rml.framework.shared.RMLException

class StdGraphMapExtractor extends GraphMapExtractor {

  /**
    * Extract.
    *
    * @param resource Resource to extract from.
    * @return
    *
    */
  override def extract(resource: RDFResource): Option[GraphMap] = {
    val mapProperties = resource.listProperties(R2RMLVoc.Property.GRAPHMAP)
    val shortcutProperties = resource.listProperties(R2RMLVoc.Property.GRAPH)
    val amount = mapProperties.size + shortcutProperties.size

    amount match {
      case 0 => None
      case 1 => if (mapProperties.nonEmpty) {
        generalExtractGraph(mapProperties.head, extractGraphMap)
      } else {
        generalExtractGraph(shortcutProperties.head, extractGraph)
      }
      case _ => throw new RMLException("Only one GraphMap allowed.")
    }

  }


  override def extractTermType(resource: RDFResource): Option[Uri] = {
    val result = super.extractTermType(resource)
    if (result.isDefined) result else Some(Uri(R2RMLVoc.Class.IRI))
  }

  def generalExtractGraph(node: RDFNode, extractFunc: RDFResource => Option[GraphMap]): Option[GraphMap] = {
    val resource = node match {
      case literal: Literal => throw new RMLException("GraphMap must be a resource.")
      case resource: RDFResource => resource
    }

    resource.uri match {
      case Uri(R2RMLVoc.Property.DEFAULTGRAPH) => None
      case _ => extractFunc(resource)
    }

  }

  def extractGraph(resource: RDFResource): Option[GraphMap] = {

    Some(GraphMap(resource.uri.value, List(), Some(resource.uri), None, None, extractTermType(resource)))

  }

  def extractGraphMap(resource: RDFResource): Option[GraphMap] = {
    val termType = extractTermType(resource)
    val template = extractTemplate(resource)
    val constant = extractConstant(resource)
    val reference = extractReference(resource)
    val functionMap = FunctionMapExtractor().extract(resource)
    Some(GraphMap(constant.getOrElse(resource.uri).value, functionMap, constant, reference, template, termType))
  }

}
