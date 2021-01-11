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
package io.rml.framework.core.extractors

import io.rml.framework.core.model.Literal
import io.rml.framework.core.model.rdf.RDFResource
import io.rml.framework.shared.RMLException

object ExtractorUtil {

  def extractLiteralFromProperty(resource: RDFResource, property: String, defaultValue: String) = {
    val properties = resource.listProperties(property);
    if (properties.isEmpty) {
      defaultValue;
    } else {
      properties.head match {
        case literal: Literal => literal.value
        case _ => defaultValue
      }
    }
  }

  def extractSingleLiteralFromProperty(resource: RDFResource, property: String): String = {
    val properties = resource.listProperties(property);
    require(properties.length == 1, resource.uri.toString + ": exactly 1 " + property + " needed.");
    properties.head match {
      case literal: Literal => literal.value
      case res: RDFResource => throw new RMLException(res.uri + ": must be a literal.")
    }
  }

  def extractSingleResourceFromProperty(resource: RDFResource, property: String): RDFResource = {
    val properties = resource.listProperties(property);
    require(properties.length == 1, resource.uri.toString + ": exactly 1 " + property + " needed.");
    properties.head match {
      case literal: Literal => throw new RMLException(resource.uri + ": " + property + " must be a resource.");
      case resource: RDFResource => resource
    }
  }

  def extractResourceFromProperty(resource: RDFResource, property: String): Option[RDFResource] = {
    val properties = resource.listProperties(property);
    if (properties.isEmpty) {
      None
    } else {
      properties.head match {
        case literal: Literal => throw new RMLException(resource.uri + ": " + property + " must be a resource.");
        case resource: RDFResource => Some(resource)
      }
    }
  }

}
