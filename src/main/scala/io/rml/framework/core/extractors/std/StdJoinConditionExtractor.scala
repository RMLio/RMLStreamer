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

import io.rml.framework.core.extractors.JoinConditionExtractor
import io.rml.framework.core.internal.Logging
import io.rml.framework.core.model.rdf.RDFResource
import io.rml.framework.core.model.{JoinCondition, Literal}
import io.rml.framework.core.vocabulary.R2RMLVoc
import io.rml.framework.shared.RMLException

class StdJoinConditionExtractor extends JoinConditionExtractor with Logging {

  /**
    * Extracts logical source from resource.
    *
    * @param node Resource to extract logical source from.
    * @return
    */
  @throws(classOf[RMLException])
  override def extract(node: RDFResource): Option[JoinCondition] = {

    logDebug(node.uri + ": Extracting join condition's parent & child.")

    val child_attributes = extractChild(node)
    val parent_attributes = extractParent(node)

    if (child_attributes.length  != parent_attributes.length) {
      throw new RMLException(node.uri + ": number of join attributes for parent and child mismatched \n" +
        s"Parent: ${parent_attributes.length} \n" +
        s"Child: ${child_attributes.length}\n")
    }

    if(child_attributes.isEmpty){
      None
    }else {
      Some(JoinCondition(child_attributes, parent_attributes))
    }

  }
  private def extractAttributes(literal: Literal): List[Literal] = {
    val attributes = literal.value
    attributes.split(",").map(attr =>  Literal(attr.trim())).toList
  }
  private def extractParent(resource: RDFResource): List[Literal] = {

    val property = R2RMLVoc.Property.PARENT
    val properties = resource.listProperties(property)

    if (properties.size != 1)
      throw new RMLException(resource.uri + ": invalid amount of parent literals (amount=" + properties.size + ", should be 1 only).")

    properties.head match {
      case literal: Literal => extractAttributes(literal)
      case resource: RDFResource => throw new RMLException(resource.uri + ": parent must be a literal.")
    }

  }

  private def extractChild(resource: RDFResource): List[Literal] = {
    val property = R2RMLVoc.Property.CHILD
    val properties = resource.listProperties(property)

    if (properties.size != 1)
      throw new RMLException(resource.uri + ": invalid amount of child literals (amount=" + properties.size + ", should be 1 only).")

    properties.head match {
      case literal: Literal => extractAttributes(literal)
      case resource: RDFResource => throw new RMLException(resource.uri + ": child must be a literal.")
    }
  }

}
