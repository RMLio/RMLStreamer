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

import java.io.File

import io.rml.framework.core.model.rdf.jena.JenaFactory
import io.rml.framework.core.model.{Literal, Uri}
import io.rml.framework.core.util.Format
import io.rml.framework.shared.ReadException

/**
  *
  */
trait RDFGraph extends RDFNode {

  /**
    *
    * @return
    */
  def uri: Uri

  override def identifier: String = this.uri.toString
  /**
    *
    * @param triple
    */
  def addTriple(triple: RDFTriple): Unit

  /**
    *
    * @param triples
    */
  def addTriples(triples: List[RDFTriple]): Unit

  /**
    *
    * @return
    */
  def listTriples: List[RDFTriple]

  /**
    * Filters the resources that are contained in the RDFGraph on the given
    * resource type. Thus, only subjects (as resources) are returned that are
    * present in triples of the following pattern:
    *
    * <b> resourceUri rdf:type resourceTypeUri </b>
    *
    * @param resourceTypeUri Type uri to filter resources on.
    * @return A filtered set of RDFResource instances.
    */
  def filterResources(resourceTypeUri: Uri): List[RDFResource]

  /**
    * Filters the resources that are contained in the RDFGraph on the given
    * property uri. Thus, only subjects (as resources) are returned that are
    * present in triples of the following pattern:
    *
    * resourceUri <b> propertyUri </b> objectUri
    *
    * @param propertyUri Type uri to filter resources on.
    * @return A filtered set of RDFResource instances.
    */
  def filterProperties(propertyUri: Uri): List[RDFResource]

  /**
    * Outputs a string containing the statements in the
    * model in the given format.
    *
    * @param format Output format
    * @return a formatted string containing the statements of the model
    */
  def write(format: Format): String

  /**
    *
    * @param file
    * @throws io.rml.framework.shared.ReadException
    */
  @throws(classOf[ReadException])
  def read(file: File): Unit

  /**
    *
    * @param dump
    * @throws io.rml.framework.shared.ReadException
    */
  @throws(classOf[ReadException])
  def read(dump: String, format: String = "TURTLE"): Unit

  /**
    * Creates an RDFResource instance from a uri.
    *
    * @param uri Uri that represents the resource.
    * @return RDFResource instance.
    */
  def createResource(uri: Uri): RDFResource

  /**
    * Creates an RDFLiteral instance from a Literal instance.
    *
    * =============================================================================================
    * ::: Developers note :::
    * RDFLiterals are used for RDF model representations,
    * Literals are used for representing models in the RML Mapping.
    * =============================================================================================
    *
    * @param literal Literal to generate an RDFLiteral from.
    * @return RDFLiteral instance.
    */
  def createLiteral(literal: Literal): RDFLiteral


  /**
    *
    * Clears the stored statements in the model
    */
  def clear():Unit

}

object RDFGraph {

  /**
    *
    * @param uri
    * @param factory
    * @return
    */
  def apply(uri: Option[Uri], factory: RDFFactory = new JenaFactory): RDFGraph = {
    factory.createGraph(uri)
  }

  /**
    *
    * @param file
    * @param factory
    * @throws io.rml.framework.shared.ReadException
    * @return
    */
  @throws(classOf[ReadException])
  def fromFile(file: File, factory: RDFFactory = new JenaFactory): RDFGraph = {
    factory.createGraph(file)
  }

}

