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

import io.rml.framework.core.extractors.{FunctionMapExtractor, GraphMapExtractor, SubjectMapExtractor}
import io.rml.framework.core.internal.Logging
import io.rml.framework.core.model.rdf.{RDFLiteral, RDFResource}
import io.rml.framework.core.model.{Literal, SubjectMap, Uri}
import io.rml.framework.core.vocabulary.R2RMLVoc
import io.rml.framework.shared.RMLException

/**
  * Extractor for extracting Subject Maps from RDFResources.
  */
class StdSubjectMapExtractor extends SubjectMapExtractor with Logging {

  /**
    * Extracts a SubjectMap from a resource.
    * It is assumed that the resource is a TriplesMap.
    *
    * @param node Resource to extract a subject map from.
    * @throws RMLException Thrown when invalid resources/literals are found.
    * @return
    */
  @throws(classOf[RMLException])
  override def extract(node: RDFResource): SubjectMap = {

    logDebug(node.uri + ": Extracting subject map.")

    val property = R2RMLVoc.Property.SUBJECTMAP
    val subjectMapResources = node.listProperties(property)

    if (subjectMapResources.size != 1)
      throw new RMLException(node.uri + ": invalid amount of subject maps.")

    val subjectMapResource = subjectMapResources.head
    subjectMapResource match {
      case resource: RDFResource => extractSubjectMapFromResource(resource)
      case literal: RDFLiteral =>
        throw new RMLException(literal.value + ": subject map must be a resource.")
    }

  }

  /**
    * Extracts properties from a subject map resource and put these together
    * to create an instance of SubjectMap.
    *
    * @param resource Resource to extract subject map properties from.
    * @throws RMLException Thrown when invalid properties are found.
    * @return Instance of SubjectMap.
    */
  @throws(classOf[RMLException])
  private def extractSubjectMapFromResource(resource: RDFResource): SubjectMap = {

    val _class = extractClass(resource)
    val reference = extractReference(resource)
    val constant = extractConstant(resource)
    val template = extractTemplate(resource)
    val termType = extractTermType(resource)
    val graphMap = GraphMapExtractor().extract(resource)

    val functionMap = FunctionMapExtractor().extract(resource)

    logDebug(resource.uri + ": Extracted from subject map" +
      ": reference -> " + reference +
      ", constant -> " + constant +
      ", template -> " + template +
      ", termType -> " + termType +
      ", graphMap -> " + graphMap +
      ", class -> " + _class)

    SubjectMap(resource.uri.toString, _class, functionMap, constant, reference, template, termType, graphMap)
  }

  override def extractTermType(resource: RDFResource): Option[Uri] = {
    val result = super.extractTermType(resource)

    result match{
      case Some(Uri(R2RMLVoc.Class.LITERAL)) => throw new RMLException("Subject cannot be literal type")
      case Some(e) => Some(e)
      case _ => Some(Uri(R2RMLVoc.Class.IRI))
    }
  }

  /**
    * Extracts class properties from subject map.
    *
    * @param resource Subject map resource.
    * @throws RMLException Thrown when an invalid subject map class resource is found.
    * @return A set containing all class Uri's.
    */
  @throws(classOf[RMLException])
  private def extractClass(resource: RDFResource): List[Uri] = {
    val property = R2RMLVoc.Property.CLASS
    val classResources = resource.listProperties(property)
    classResources.map {
      case resource: RDFResource => resource.uri
      case literal: Literal =>
        throw new RMLException(literal.value + ": not a valid subject map class resource.")
    }
  }

}
