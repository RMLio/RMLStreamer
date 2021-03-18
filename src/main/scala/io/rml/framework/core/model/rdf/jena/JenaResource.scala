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

package io.rml.framework.core.model.rdf.jena

import io.rml.framework.core.model.Uri
import io.rml.framework.core.model.rdf.{RDFLiteral, RDFNode, RDFResource}
import io.rml.framework.core.vocabulary.RDFVoc
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.apache.jena.rdf.model.{RDFList, Resource}

import scala.collection.JavaConverters._

class JenaResource(val resource: Resource) extends RDFResource {

  private val model = resource.getModel

  override def uri: Uri = {
    val _uri = resource.getURI // this can be null if it's a blank node
    if (_uri == null) Uri(resource.getId.getLabelString) // use the internal id as Uri if it's a blank node
    else Uri(_uri)
  }

  override def listProperties(propertyUri: String): List[RDFNode] = {


    val property = resource.getModel.createProperty(propertyUri)
    val properties = resource.listProperties(property).asScala
    properties.map(property => JenaNode(property.getObject)).toList
  }

  override def addProperty(property: String, resource: String): RDFResource = {
    this.resource.addProperty(model.createProperty(property),
      model.createResource(resource))
    this
  }

  /**
    * Two JenaResource objects are only equal if the resource member of the objects are equal.
    *
    * @param that a scala object
    * @return
    */
  override def equals(that: Any): Boolean = {
    that match {
      case that: JenaResource => this.resource == that.resource && this.model == that.model
      case _ => false
    }
  }

  /**
    * HashCode of the JenaResource should be equal to the hasCode of the containing resource and model
    *
    * @return
    */

  override def hashCode(): Int =    {

    new HashCodeBuilder(7,31)
      .append(this.resource)
      .append(this.model)
      .toHashCode
  }

  /**
    *
    * @param property
    * @param resource
    */
  override def addProperty(property: String, resource: RDFResource): RDFResource = {
    this.resource.addProperty(model.createProperty(property),
      model.createResource(resource.uri.toString))
    this
  }

  /**
    *
    * @param property
    * @param literal
    */
  override def addLiteral(property: String, literal: String): RDFResource = {
    this.resource.addLiteral(model.createProperty(property), literal)
    this
  }

  /**
    *
    * @param property
    * @param literal
    */
  override def addLiteral(property: String, literal: RDFLiteral): RDFResource = {
    this.resource.addLiteral(model.createProperty(property), literal.toString)
    this
  }

  override def getType: Option[Uri] = {
    val types: Seq[RDFNode] = listProperties(RDFVoc.Property.TYPE)
    types.size match {
      case 0 => {
        // no type defined
        this.logWarning("No rdf:type defined for resource.")
        None
      }
      case 1 => {
        require(types.head.isInstanceOf[RDFResource], "Type must be a resource.")
        Some(types.head.asInstanceOf[RDFResource].uri)
      }
      case _ => {
        // multiple types defined...
        // TODO: what if there are multiple types?
        this.logWarning("Multiple rdf:types defined for resource.")
        None
      }
    }

  }

  override def getList: List[RDFNode] = {
    try {
      val list = resource.as(classOf[RDFList]).asJavaList()
      list.asScala.toList.map(JenaNode(_))
    } catch {
      case _: Exception => List(JenaNode(resource))
    }

  }
}

object JenaResource {
  def apply(resource: Resource): JenaResource = new JenaResource(resource)
}
