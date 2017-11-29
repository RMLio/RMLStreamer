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

package io.rml.framework.core.model.rdf.jena

import io.rml.framework.core.model.Uri
import io.rml.framework.core.model.rdf.{RDFLiteral, RDFNode, RDFResource}
import org.apache.jena.rdf.model.Resource
import scala.collection.JavaConverters._

class JenaResource(resource: Resource) extends RDFResource{

  private val model = resource.getModel

  override def uri: Uri = {
    val _uri = resource.getURI // this can be null if it's a blank node
    if(_uri == null) Uri(resource.getId.getLabelString) // use the internal id as Uri if it's a blank node
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

}

object JenaResource {
  def apply(resource: Resource): JenaResource = new JenaResource(resource)
}
