/*
 * Copyright (c) 2017 Ghent University - imec
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.rml.framework.core.extractors.std

import io.rml.framework.core.extractors.{JoinConditionExtractor, ObjectMapExtractor, TripleMapExtractor}
import io.rml.framework.core.model._
import io.rml.framework.core.model.rdf.{RDFLiteral, RDFResource}
import io.rml.framework.core.vocabulary.RMLVoc
import io.rml.framework.shared.RMLException

class StdObjectMapExtractor extends ObjectMapExtractor {
  /**
    * Extract.
    *
    * @param node Node to extract from.
    * @return
    */
  override def extract(node: RDFResource): List[ObjectMap] = {
    extractObjects(node) ::: extractObjectMaps(node)
  }

  /**
    * Extract objects. These are shortcuts for object maps with constants.
    * @param resource
    * @return
    */
  private def extractObjects(resource: RDFResource): List[ObjectMap] = {
    val property = RMLVoc.Property.OBJECT
    val properties = resource.listProperties(property)

    // iterates over predicates, converts these to predicate maps as blanks
    properties.map {
      case literal: RDFLiteral =>
        ObjectMap(Blank(), constant = Some(Uri(literal.value)))
      case resource: RDFResource =>
        ObjectMap(Blank(), constant = Some(resource.uri))
    }
  }

  /**
    * Extract object maps.
    * @param resource
    * @return
    */
  private def extractObjectMaps(resource: RDFResource): List[ObjectMap] = {
    val property = RMLVoc.Property.OBJECTMAP
    val properties = resource.listProperties(property)

    // iterates over predicatesMaps
    properties.map {
      case literal: RDFLiteral =>
        throw new RMLException(literal.toString + ": Cannot convert literal to predicate map.")
      case resource: RDFResource => extractObjectMap(resource)
    }

  }

  /**
    * Extract a single object map.
    * @param resource
    * @return
    */
  private def extractObjectMap(resource: RDFResource) : ObjectMap = {
    val termType = extractTermType(resource)
    val template = extractTemplate(resource)
    val constant = extractConstant(resource)
    val reference = extractReference(resource)
    val parentTriplesMap = extractParentTriplesMap(resource)
    val joinCondition = extractJoinCondition(resource)

    ObjectMap(resource.uri, constant, reference, template, termType, parentTriplesMap, joinCondition)
  }

  private def extractParentTriplesMap(resource: RDFResource) : Option[TripleMap] = {

    val property = RMLVoc.Property.PARENTTRIPLESMAP
    val properties = resource.listProperties(property)

    if(properties.size > 1)
      throw new RMLException(resource.uri + ": invalid amount of parent triple maps.")
    if(properties.isEmpty) return None

    properties.head match {
      case resource: RDFResource => TripleMapExtractor().extractTripleMapProperties(resource)
                                                        .flatMap(tm => Some(ParentTriplesMap(tm))) // transform to PTM
      case literal: Literal =>
        throw new RMLException(literal.toString + ": invalid parent triple map.")
    }

  }

  private def extractJoinCondition(resource: RDFResource) : Option[JoinCondition] = {
    val property = RMLVoc.Property.JOINCONDITION
    val properties = resource.listProperties(property)

    if(properties.size > 1)
      throw new RMLException(resource.uri + ": invalid amount of join conditions (amount=" + properties.size + ").")
    if(properties.isEmpty) return None

    properties.head match {
      case resource: RDFResource => JoinConditionExtractor().extract(resource)
      case literal: Literal =>
        throw new RMLException(literal.toString + ": invalid join condition, must be a resource.")
    }
  }
}
