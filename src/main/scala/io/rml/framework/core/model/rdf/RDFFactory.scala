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

import io.rml.framework.core.model.{Entity, TermNode, Uri}
import io.rml.framework.shared.ReadException

/**
  * Trait that creates the RDF model structures that are used by the RML Framework.
  */
trait RDFFactory {

  /**
    * Creates an empty instance of RDFGraph with an optional uri.
    *
    * @param uri Optional uri which represents the uri of the graph.
    * @return RDFGraph instance.
    */
  def createGraph(uri: Option[Uri]): RDFGraph

  /**
    * Creates an instance of RDFGraph by loading a file. The file must be in a valid RDF format.
    *
    * @param file RDF file to create an RDFGraph from.
    * @throws ReadException Thrown when a fault occurred whilst loading the file.
    * @return RDFGraph instance.
    */
  @throws(classOf[ReadException])
  def createGraph(file: File): RDFGraph

  /**
    * Creates a RDFTriple instance from a given subject, predicate and object.
    *
    * @param subject   Subject uri of the triple.
    * @param predicate Predicate uri of the triple.
    * @param _object   Object value of the triple. This can be either a Literal or a Uri.
    * @return A RDFTriple instance.
    */
  def createTriple(subject: TermNode, predicate: Uri, _object: Entity): RDFTriple

}