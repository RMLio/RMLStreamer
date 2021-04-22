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

import io.rml.framework.core.extractors.{LogicalTargetExtractor, ResourceExtractor}
import io.rml.framework.core.model.rdf.RDFResource
import io.rml.framework.core.model.{Entity, Literal, LogicalTarget, Uri}
import io.rml.framework.core.vocabulary.{R2RMLVoc, RMLVoc}
import io.rml.framework.shared.RMLException

import scala.util.matching.Regex

/**
  * Abstract extractor for extracting common properties of term maps.
  *
  * @tparam T
  */
abstract class TermMapExtractor[T] extends ResourceExtractor[T] {
  lazy private val logicalTargetExtractor: LogicalTargetExtractor = LogicalTargetExtractor()

  protected def extractLogicalTargets(node: RDFResource): Set[LogicalTarget] = {
    logicalTargetExtractor.extract(node)
  }

  /**
    * Extracts template property from a resource.
    *
    * @param resource Resource to extract from.
    * @throws RMLException thrown when an invalid template is found.
    * @return
    */
  @throws(classOf[RMLException])
  protected def extractTemplate(resource: RDFResource): Option[Literal] = {
    val property = R2RMLVoc.Property.TEMPLATE
    val properties = resource.listProperties(property)

    if (properties.size > 1)
      throw new RMLException(resource.uri + ": invalid amount of template properties.")
    if (properties.isEmpty) return None

    properties.head match {
      case literal: Literal => {
        // check if a template is found
        if (TermMapExtractor.isCorrectTemplate(literal)) Some(literal)
        else throw new RMLException(literal.toString + ": No template found.")
      }
      case resource: RDFResource =>
        throw new RMLException(resource.uri + ": invalid term map template.")
    }
  }

  /**
    * Extracts reference property from a resource.
    *
    * @param resource Resource to extract from.
    * @throws RMLException thrown when an invalid reference is found.
    * @return
    */
  @throws(classOf[RMLException])
  protected def extractReference(resource: RDFResource): Option[Literal] = {
    val property = RMLVoc.Property.REFERENCE
    val properties = resource.listProperties(property)

    if (properties.size > 1)
      throw new RMLException(resource.uri + ": invalid amount of reference properties.")
    if (properties.isEmpty) return None

    properties.head match {
      case literal: Literal => Some(literal)
      case resource: RDFResource =>
        throw new RMLException(resource.uri + ": invalid term map reference.")
    }
  }

  /**
    * Extracts constant property from a resource.
    *
    * @param resource Resource to extract from.
    * @throws RMLException thrown when an invalid constant is found.
    * @return
    */
  @throws(classOf[RMLException])
  protected def extractConstant(resource: RDFResource): Option[Entity] = {
    val property = R2RMLVoc.Property.CONSTANT
    val properties = resource.listProperties(property)

    if (properties.size > 1)
      throw new RMLException(resource.uri + ": invalid amount of constant properties.")
    if (properties.isEmpty) return None

    properties.head match {
      case literal: Literal => Some(literal)
      case resource: RDFResource => Some(resource.uri)
    }
  }

  /**
    * Extracts term type property from a resource.
    *
    * @param resource Resource to extract from.
    * @throws RMLException thrown when an invalid term type is found.
    * @return
    */
  @throws(classOf[RMLException])
  protected def extractTermType(resource: RDFResource): Option[Uri] = {
    val property = R2RMLVoc.Property.TERMTYPE
    val properties = resource.listProperties(property)

    if (properties.size > 1)
      throw new RMLException(resource.uri + ": invalid amount of term type properties.")
    if (properties.isEmpty) return None

    properties.head match {
      case resource: RDFResource => Some(resource.uri)
      case literal: Literal =>
        throw new RMLException(literal.value + ": invalid term map term type.")
    }

  }

}

object TermMapExtractor {

  val TEMPLATE_REGEX: Regex = "(.*)(\\{.*\\})(.*)".r // in Java: escape brackets two times

  /**
    * Check if given literal is a correct template.
    *
    * @param literal
    * @return
    */
  def isCorrectTemplate(literal: Literal): Boolean = {
    literal.toString match {
      case TermMapExtractor.TEMPLATE_REGEX(prefix, reference, suffix) => true
      case _ => false
    }
  }

}
