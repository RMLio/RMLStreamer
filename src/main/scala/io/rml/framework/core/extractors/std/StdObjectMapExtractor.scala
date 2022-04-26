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

import io.rml.framework.core.extractors.{FunctionMapExtractor, JoinConditionExtractor, JoinConfigMapCache, JoinConfigMapExtractor, ObjectMapExtractor}
import io.rml.framework.core.model._
import io.rml.framework.core.model.rdf.{RDFLiteral, RDFResource}
import io.rml.framework.core.util.Util
import io.rml.framework.engine.windows.WindowType
import io.rml.framework.core.vocabulary.{FunVoc, R2RMLVoc, RMLVoc}
import io.rml.framework.shared.RMLException
import io.rml.framework.core.vocabulary.RMLSVoc

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
    *
    * @param resource
    * @return
    */
  private def extractObjects(resource: RDFResource): List[ObjectMap] = {
    val property = R2RMLVoc.Property.OBJECT
    val properties = resource.listProperties(property)

    // iterates over predicates, converts these to predicate maps as blanks
    properties.map {
      case literal: RDFLiteral =>
        ObjectMap("", constant = Some(Literal(literal.value)), termType = Some(Uri(R2RMLVoc.Class.LITERAL)), logicalTargets = Set())
      case resource: RDFResource =>
        ObjectMap("", constant = Some(resource.uri), termType = Some(Uri(R2RMLVoc.Class.IRI)), logicalTargets = Set())
    }
  }

  /**
    * Extract object maps.
    *
    * @param resource
    * @return
    */
  private def extractObjectMaps(resource: RDFResource): List[ObjectMap] = {
    this.logDebug("extractObjectMaps(resource)")
    val property = R2RMLVoc.Property.OBJECTMAP
    val properties = resource.listProperties(property)

    // iterates over predicatesMaps
    properties.flatMap {
      case literal: RDFLiteral =>
        throw new RMLException(literal.toString + ": Cannot convert literal to predicate map.")
      case resource: RDFResource => Some(extractObjectMap(resource))
      }


  }

  /**
    * Extract a single object map.
    *
    * @param resource
    * @return
    */
  private def extractObjectMap(resource: RDFResource): ObjectMap = {

    require(resource != null, "Resource can't be null.")

    val termType = extractTermType(resource)
    val template = extractTemplate(resource)
    val constant = extractConstant(resource)
    val reference = extractReference(resource)
    val parentTriplesMap = extractParentTriplesMap(resource)
    val joinCondition = extractJoinCondition(resource)
    val language = extractLanguage(resource)
    val datatype = extractDatatype(resource)
    val functionMap = FunctionMapExtractor().extract(resource)
    val windowType = extractWindowType(resource)
    val joinConfigMap = extractJoinConfigMap(resource, windowType)
    val logicalTargets = extractLogicalTargets(resource)
    ObjectMap(resource.uri.identifier, functionMap, constant, 
    reference, template, termType, 
    datatype, language, windowType, joinConfigMap, parentTriplesMap, 
    joinCondition, logicalTargets)
  }


  def extractDatatype(resource: RDFResource): Option[Uri] = {
    val property = R2RMLVoc.Property.DATATYPE
    val properties = resource.listProperties(property)

    if (properties.size > 1)
      throw new RMLException(resource.uri + ": invalid amount of reference properties.")
    if (properties.isEmpty) return None

    properties.head match {
      case literal: Literal => throw new RMLException(resource.uri + ": invalid data type.")
      case resource: RDFResource => Some(resource.uri)
    }
  }

  def extractWindowType(resource: RDFResource):Option[WindowType] = {
    val property = RMLSVoc.Property.WINDOW_TYPE; 
    val properties = resource.listProperties(property)

    if (properties.size > 1)
      throw new RMLException(resource.uri + ": invalid amount of window type properties.")
    if (properties.isEmpty) return None

    val windowTypeProperty = properties.head match {
      case literal: Literal => throw new RMLException(resource.uri + ": invalid window type.")
      case resource: RDFResource => Some(resource.uri.toString)
      case _ => None
    }

    windowTypeProperty.flatMap( WindowType.fromUri)

  }
  override def extractTermType(resource: RDFResource): Option[Uri] = {
    val result = super.extractTermType(resource)
    if (result.isDefined) result else {

      //if the resource has rr:constant, return the type of the node referred by rr:constant.
      val constantValue = resource.listProperties(R2RMLVoc.Property.CONSTANT)

      if (constantValue.nonEmpty) {

        constantValue.head match {
          case literal: Literal => Some(Uri(R2RMLVoc.Class.LITERAL))
          case _ => Some(Uri(R2RMLVoc.Class.IRI))
        }

      } else {
        // the term type is Literal when one of the following is true
        //  - the resource is a reference-based term map
        //  - the resource contains a referenceFormulation
        //  - the resource has a datatype property
        //  - the resource has a functionValue property // TODO: verify
        val elements =
          resource.listProperties(RMLVoc.Property.REFERENCE) ++
          resource.listProperties(RMLVoc.Property.REFERENCEFORMULATION) ++
          resource.listProperties(R2RMLVoc.Property.DATATYPE) ++
          resource.listProperties(FunVoc.Fnml.Property.FUNCTIONVALUE)

        if (elements.nonEmpty) Some(Uri(R2RMLVoc.Class.LITERAL))
        else Some(Uri(R2RMLVoc.Class.IRI))
      }
    }
  }

  def extractLanguage(resource: RDFResource): Option[Literal] = {
    val property = R2RMLVoc.Property.LANGUAGE
    val properties = resource.listProperties(property)

    if (properties.size > 1)
      throw new RMLException(resource.uri + ": invalid amount of language properties.")
    if (properties.isEmpty) return None

    val languageLiteral = properties.head match {
      case literal: Literal => Some(literal)
      case resource: RDFResource => throw new RMLException(resource.uri + ": invalid language type.")
    }
    val tag = languageLiteral.get.value

    if (!Util.isValidrrLanguage(tag))
      throw new RMLException(s"Language tag '$tag' does not conform to BCP 47 standards")


    languageLiteral
  }

  private def extractJoinConfigMap(resource: RDFResource, windowType: Option[WindowType]): Option[String] = {
    val property = RMLSVoc.Property.JOIN_CONFIG
    val properties = resource.listProperties(property)

    if (properties.size > 1)
      throw new RMLException(resource.uri + ": invalid amount of join config maps.")
    if (properties.isEmpty) return None

    val configMapOption = JoinConfigMapExtractor(windowType).extract(resource)
    configMapOption.foreach(configMap => JoinConfigMapCache.put(configMap.toString, configMap))

    properties.head match {
      case resource: RDFResource => Some(resource.toString)
      case literal: Literal  =>
        throw new RMLException(literal.toString + ": invalid join config map.")

    }
  }

  private def extractParentTriplesMap(resource: RDFResource): Option[String] = {

    val property = R2RMLVoc.Property.PARENTTRIPLESMAP
    val properties = resource.listProperties(property)

    if (properties.size > 1)
      throw new RMLException(resource.uri + ": invalid amount of parent triple maps.")
    if (properties.isEmpty) return None

    properties.head match {
      case resource: RDFResource => Some(resource.value)
      case literal: Literal =>
        throw new RMLException(literal.toString + ": invalid parent triple map.")
    }

  }

  private def extractJoinCondition(resource: RDFResource): Option[JoinCondition] = {
    val property = R2RMLVoc.Property.JOINCONDITION
    val properties = resource.listProperties(property)

    if (properties.size > 1)
      throw new RMLException(resource.uri + ": invalid amount of join conditions (amount=" + properties.size + ").")
    if (properties.isEmpty) return None

    properties.head match {
      case resource: RDFResource => JoinConditionExtractor().extract(resource)
      case literal: Literal =>
        throw new RMLException(literal.toString + ": invalid join condition, must be a resource.")
    }
  }
}
