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

import io.rml.framework.core.model.rdf.{RDFLiteral, RDFResource}
import io.rml.framework.core.model.{FunctionMap, Uri}
import io.rml.framework.core.vocabulary.RMLVoc
import io.rml.framework.shared.RMLException

case class FunctionMapExtractor() extends ResourceExtractor[List[FunctionMap]] {

  lazy val triplesMapExtractor: TriplesMapExtractor = TriplesMapExtractor()

  /**
    * Extract.
    *
    * @param node Node to extract from.
    * @return
    */
  override def extract(node: RDFResource): List[FunctionMap] = {
    val properties = node.listProperties(RMLVoc.Property.OBJECTMAP)
    properties.flatMap {
      case literal: RDFLiteral =>
        throw new RMLException(literal.toString +
          ": A literal cannot be converted to a predicate object map")

      case resource: RDFResource =>
        resource.getType match {
          case Some(Uri(RMLVoc.Class.FUNCTIONTERMMAP)) => Some(extractFunctionMap(resource))
          case _ => None
        }

    }
  }

  private def extractFunctionMap(resource: RDFResource): FunctionMap = {
    val functionValues = resource.listProperties(RMLVoc.Property.FUNCTIONVALUE)

    require(functionValues.size == 1, "Only 1 function value allowed.")
    require(functionValues.head.isInstanceOf[RDFResource], "FunctionValue must be a resource.")

    val functionValue = functionValues.head.asInstanceOf[RDFResource]
    val triplesMap = triplesMapExtractor.extractTriplesMapProperties(functionValue)
    require(triplesMap.isDefined)
    FunctionMap(functionValue.uri.toString, triplesMap.get)
  }

}
