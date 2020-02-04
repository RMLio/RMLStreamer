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

package io.rml.framework.core.util

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

import org.apache.jena.query.{Dataset, DatasetFactory}
import org.apache.jena.rdf.model.Model
import org.apache.jena.riot.{Lang, RDFFormat, RDFParser}

import scala.collection.mutable


object JenaUtil {

  def format(format: Format): String = {
    format match {
      case Turtle => "TURTLE"
      case NTriples => "N-TRIPLES"
      case JSON_LD => "JSON-LD"
      case NQuads => "N-QUADS"
    }
  }

  def toRDFFormat(format: Format): RDFFormat = {
    format match {
      case Turtle => RDFFormat.TURTLE
      case NTriples => RDFFormat.NTRIPLES
      case JSON_LD => RDFFormat.JSONLD
    }
  }

  def toLang(format: Format): Lang = {
    format match {
      case Turtle => Lang.TURTLE
      case NTriples => Lang.NTRIPLES
      case NQuads => Lang.NQUADS
      case JSON_LD => Lang.JSONLD
    }
  }

  def readDataset(input: String, baseIRI: String, format: Format): Dataset = {
    val dataset = DatasetFactory.create()
    RDFParser.fromString(input)
      .base(baseIRI)
      .lang(toLang(format))
      .parse(dataset)
    dataset
  }

  def toModels(dataset: Dataset): List[(Model, String)] = {
    var models = mutable.MutableList[(Model, String)]()
    models = models ++ List((dataset.getDefaultModel, ""))
    val iter = dataset.listNames();
    while (iter.hasNext) {
      val name = iter.next()
      val model = dataset.getNamedModel(name)
      models = models ++ List((model, name))
    }
    models.toList
  }

  def toString(model: Model): String = {
    val bos = new ByteArrayOutputStream
    model.write(bos, Lang.NQUADS.getName)
    bos.toString(StandardCharsets.UTF_8.name)
  }
}
