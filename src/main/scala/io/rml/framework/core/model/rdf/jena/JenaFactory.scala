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

import java.io.File

import io.rml.framework.core.model.rdf._
import io.rml.framework.core.model._
import io.rml.framework.shared.ReadException
import org.apache.jena.rdf.model.{Model, ModelFactory}

/**
  *
  * Implementation of the RDFFactory trait by using the Jena API.
  *
  * ===============================================================================================
  * ::: Developers note :::
  * This class makes use of Scala implicits and the type pattern. If this turns out to be difficult
  * to maintain, this class needs to be refactored to a classic adapter pattern.
  * ===============================================================================================
  *
  */
class JenaFactory extends RDFFactory {

  // used for creating statements so not always a new instance is created
  private val _factoryModel : Model = ModelFactory.createDefaultModel()

  override def createGraph(uri: Option[Uri]) : RDFGraph = {
    JenaGraph(ModelFactory.createDefaultModel())
      .withUri(uri.orNull)
  }

  @throws(classOf[ReadException])
  override def createGraph(file: File) : RDFGraph = {
    val model = JenaGraph(ModelFactory.createDefaultModel())
                  .withUri(Uri(file.getName))
    model.read(file)
    model
  }

  override def createTriple(subject: TermNode,
                            predicate: Uri,
                            _object: Value) : RDFTriple = {
    val stringRepresentation = subject match {
      case  uri: Uri => uri.toString
      case _ => null
    }
    val subjectNode = _factoryModel.createResource(stringRepresentation)
    val predicateNode = _factoryModel.createProperty(predicate.toString)
    val objectNode = _object match {
      case o : Literal => _factoryModel.createLiteral(o.toString)
      case o : Uri => _factoryModel.createResource(o.toString)
    }
    val statement = _factoryModel.createStatement(subjectNode, predicateNode, objectNode)
    JenaTriple(statement)
  }

}
