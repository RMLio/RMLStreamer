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

import io.rml.framework.core.extractors.{FunctionMapExtractor, PredicateMapExtractor}
import io.rml.framework.core.model.rdf.{RDFLiteral, RDFResource}
import io.rml.framework.core.model.{PredicateMap, Uri}
import io.rml.framework.core.vocabulary.R2RMLVoc
import io.rml.framework.shared.RMLException


class StdPredicateMapExtractor() extends PredicateMapExtractor {
  /**
    * Extract.
    *
    * @param node Node to extract from.
    * @return
    */
  override def extract(node: RDFResource): List[PredicateMap] = {
    extractPredicates(node) ::: extractPredicateMaps(node)
  }

  /**
    * Extract predicates. These are shortcuts for predicate maps with constants.
    *
    * @param resource
    * @return
    */
  private def extractPredicates(resource: RDFResource): List[PredicateMap] = {
    val property = R2RMLVoc.Property.PREDICATE
    val properties = resource.listProperties(property)

    // iterates over predicates, converts these to predicate maps as blanks
    properties.map {
      case literal: RDFLiteral =>
        PredicateMap("", constant = Some(Uri(literal.value)), termType = Some(Uri(R2RMLVoc.Class.IRI)), logicalTargets = Set())
      case resource: RDFResource =>
        PredicateMap("", constant = Some(resource.uri), termType = Some(Uri(R2RMLVoc.Class.IRI)), logicalTargets = Set())
    }
  }

  /**
    * Extract predicate maps.
    *
    * @param resource
    * @return
    */
  private def extractPredicateMaps(resource: RDFResource): List[PredicateMap] = {
    val property = R2RMLVoc.Property.PREDICATEMAP
    val properties = resource.listProperties(property)

    // iterates over predicatesMaps
    properties.map {
      case literal: RDFLiteral =>
        throw new RMLException(literal.toString + ": Cannot convert literal to predicate map.")
      case resource: RDFResource => extractPredicateMap(resource)
    }

  }

  /**
    * Extract a single predicate map.
    *
    * @param resource
    * @return
    */
  private def extractPredicateMap(resource: RDFResource): PredicateMap = {
    val termType = Some(Uri(R2RMLVoc.Class.IRI)) // this is always the case as defined by the spec
    val template = extractTemplate(resource)
    val constant = extractConstant(resource)
    val reference = extractReference(resource)
    val functionMap = FunctionMapExtractor().extract(resource)
    val logicalTargets = extractLogicalTargets(resource)

    PredicateMap(resource.uri.value, functionMap, constant, reference, template, termType, logicalTargets)
  }

}
