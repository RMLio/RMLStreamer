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

import io.rml.framework.core.internal.Logging
import io.rml.framework.core.model.rdf.{RDFGraph, RDFLiteral, RDFResource, RDFTriple}
import io.rml.framework.core.model.{Literal, Uri}
import io.rml.framework.core.util.{Format, JenaUtil, Util}
import io.rml.framework.core.vocabulary.{Namespaces, RDFVoc}
import io.rml.framework.shared.{RMLException, ReadException}
import org.apache.jena.rdf.model.{Model, ModelFactory, Statement}
import org.apache.jena.riot.RDFDataMgr

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, File, InputStream}
import java.nio.charset.StandardCharsets
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
    val jenaFormat = JenaUtil.toRDFFormat(format)
    RDFDataMgr.write(stream,model, jenaFormat)
    // if we ever want to control JSON-LD options (needed for dyversify)
    /*format match {
      case JSON_LD => {
        import com.github.jsonldjava.core.JsonLdOptions
        val opts = new JsonLdOptions
        opts.setCompactArrays(false)
        import org.apache.jena.riot.JsonLDWriteContext
        val ctx = new JsonLDWriteContext
        ctx.setOptions(opts)
        RDFWriter.create()
            .format(jenaFormat)
            .source(model)
            .context(ctx)
            .build()
          .output(stream)
      }
      case _ => {
        RDFDataMgr.write(stream, model, jenaFormat)
      }
    }*/
    stream.toString("UTF-8")
  }

  @throws(classOf[ReadException])
  override def read(dump: String, baseIRI: Option[String], format: Format): Unit = {
    val stream = new ByteArrayInputStream(dump.getBytes(StandardCharsets.UTF_8))
    read(stream, baseIRI, format)
  }

  @throws(classOf[ReadException])
  override def read(file: File, baseIRI: Option[String], format: Format): Unit = {
    val inputStream = Util.getFileInputStream(file)
    read(inputStream, baseIRI, format)
    withUri(Uri(file.getName)) // overwrite the graph uri with the file path
  }

  private def read(in: InputStream, baseIRI: Option[String], format: Format): Unit = {
    val bIri = baseIRI match {
      case Some(iri) => iri
      case None => null
    }
    model.removeAll();

    // load known prefixes
    Namespaces.iterator().toIterable.foreach(prefix2Uri => {
       model.setNsPrefix(prefix2Uri._1, prefix2Uri._2);
    });

    Util.tryWith(in) {
      in => model.read(in, bIri, JenaUtil.format(format))
    }
    logModelWhenDebugEnabled()
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
    val subject = model.createResource(triple.subject.uri.value)
    val predicate = model.createProperty(triple.predicate.uri.value)
    val _object = model.createResource(triple.`object`.value)
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

  override def value: String = ???  // this doesn't make much sense. 
}

object JenaGraph {

  def apply(model: Model): JenaGraph = new JenaGraph(model)
  def apply():JenaGraph = JenaGraph(ModelFactory.createDefaultModel())
}
