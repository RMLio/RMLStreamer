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

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, File, OutputStream}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}

import io.rml.framework.core.internal.Logging
import io.rml.framework.core.model.rdf.{RDFGraph, RDFLiteral, RDFResource, RDFTriple}
import io.rml.framework.core.model.{Literal, Uri}
import io.rml.framework.core.util.{Format, JenaUtil, Turtle, Util}
import io.rml.framework.core.vocabulary.RDFVoc
import io.rml.framework.shared.{RMLException, ReadException}
import org.apache.commons.lang3.StringUtils
import org.apache.jena.rdf.model.{Model, ModelFactory, Statement}
import org.apache.jena.riot.{RDFDataMgr, RDFFormat}
import org.apache.jena.shared.JenaException

import scala.collection.JavaConverters._

class JenaGraph(model: Model) extends RDFGraph with Logging {

  def withUri(uri: Uri): RDFGraph = {
    if (uri == null) { // deprecated check
      throw new RMLException("Conversion to RDFGraph not allowed without a set URI.")
    }
    _uri = uri
    this
  }

  override def filterResources(_type: Uri): List[RDFResource] = {
    val property = model.createProperty(RDFVoc.Property.TYPE)
    val _object = model.createResource(_type.toString)
    val resources = model.listResourcesWithProperty(property, _object).asScala
    resources.map(resource => JenaResource(resource)).toList
  }

  override def uri: Uri = _uri

  override def addTriple(triple: RDFTriple): Unit = {
    val statement = extractStatementFromTriple(triple)
    model.add(statement)
  }

  override def addTriples(triples: List[RDFTriple]): Unit = {
    val statements: java.util.List[Statement] = triples.map(extractStatementFromTriple).asJava
    model.add(statements)
  }

  override def listTriples: List[RDFTriple] = {
    model.listStatements().asScala.map(JenaTriple(_)).toList
  }

  override def write(format: Format): String = {
    val stream = new ByteArrayOutputStream()
    RDFDataMgr.write(stream,model, JenaUtil.toRDFFormat(format))
    stream.toString("UTF-8")
  }

  @throws(classOf[ReadException])
  override def read(dump: String, format: String = "TURTLE"): Unit = {
    val stream = new ByteArrayInputStream(dump.getBytes(StandardCharsets.UTF_8))
    try {

      model.read(stream, null, format)
      logModelWhenDebugEnabled()
    } catch {
      case jenaException: JenaException =>
        logDebug(jenaException.toString)
        throw new ReadException(jenaException.getMessage)
    }
  }

  @throws(classOf[ReadException])
  override def read(file: File): Unit = {
    try {
      val protocol = "file://" // needed for loading from a file

      val inputStream = Util.getFileInputStream(file)
      val baseStream = Util.getFileInputStream(file)


      val baseUrl = Util.getBaseDirective(baseStream)

      RDFDataMgr.read(model, inputStream,JenaUtil.toRDFFormat(Turtle).getLang)

      withUri(Uri(file.getName)) // overwrite the graph uri with the file path
      logModelWhenDebugEnabled()
    } catch {
      case jenaException: JenaException =>
        logDebug(jenaException.toString)
        throw new ReadException(jenaException.getMessage)
    }
  }


  /**
    * Creates an RDFResource instance from a uri.
    *
    * @param uri Uri that represents the resource.
    * @return RDFResource instance.
    */
  override def createResource(uri: Uri): RDFResource = {
    val resource = model.createResource(uri.toString)
    JenaResource(resource)
  }

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
  override def createLiteral(literal: Literal): RDFLiteral = {
    val _literal = model.createLiteral(literal.toString)
    JenaLiteral(_literal)
  }

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
  override def filterProperties(propertyUri: Uri): List[RDFResource] = {
    val property = model.createProperty(propertyUri.toString)
    val resources = model.listResourcesWithProperty(property).asScala
    resources.map(resource => JenaResource(resource)).toList
  }

  /**
    * Clears all the statements stored in the model
    */
  override def clear(): Unit = {
      model.removeAll()
  }

  ////////////////////////////////////////////////
  // Private
  ////////////////////////////////////////////////

  // uri can change state
  private var _uri: Uri = _

  private def extractStatementFromTriple(triple: RDFTriple): Statement = {
    val subject = model.createResource(triple.subject.uri.toString)
    val predicate = model.createProperty(triple.predicate.uri.toString)
    val _object = model.createResource(triple.`object`.toString)
    model.createStatement(subject, predicate, _object)
  }

  private def logModelWhenDebugEnabled(): Unit = {
    if (isDebugEnabled) {
      logDebug("Loading triples into model:")
      val statements = model.listStatements().asScala.toSet
      statements.foreach(statement => logDebug(statement.toString))
      logDebug(statements.size + " statements loaded.")
    }
  }

}

object JenaGraph {

  def apply(model: Model): JenaGraph = new JenaGraph(model)
  def apply():JenaGraph = JenaGraph(ModelFactory.createDefaultModel())
}
