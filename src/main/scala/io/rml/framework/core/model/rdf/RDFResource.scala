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

package io.rml.framework.core.model.rdf

import io.rml.framework.core.model.{Resource, Uri}
/**
  * This trait represents a resource that is able to query an underlying RDF model.
  * Different third-party libraries can be used by implementing implicits.
  */
trait RDFResource extends Resource with RDFNode {

  /**
    *
    * @param property
    * @return
    */
  def listProperties(property: String) : List[RDFNode]

  /**
    *
    * @param property
    * @param resource
    */
  def addProperty(property: String, resource: String) : RDFResource

  /**
    *
    * @param property
    * @param resource
    */
  def addProperty(property: String, resource: RDFResource) : RDFResource

  /**
    *
    * @param property
    * @param literal
    */
  def addLiteral(property: String, literal: String) : RDFResource

  /**
    *
    * @param property
    * @param literal
    */
  def addLiteral(property: String, literal: RDFLiteral) : RDFResource

}

object RDFResource {

  /**
    * Creates instances of RDFResources and adds these automatically to the given graph.
    * If the graph is not given, the compiler will automatically look for an implicit
    * instance of a graph. If no graph can be found, this method will fail.
    * @param uri Uri of the resource that will be created.
    * @param graph An implicit instance of graph must be in scope!
    * @return
    */
  def apply(uri: Uri)(implicit graph: RDFGraph): RDFResource = {
    graph.createResource(uri)
  }

  /**
    * Creates instances of RDFResources and adds these automatically to the given graph.
    * If the graph is not given, the compiler will automatically look for an implicit
    * instance of a graph. If no graph can be found, this method will fail.
    * @param uri Uri of the resource that will be created.
    * @param graph An implicit instance of graph must be in scope!
    * @return
    */
  def apply(uri: String)(implicit graph: RDFGraph): RDFResource = {
    graph.createResource(Uri(uri))
  }

}
