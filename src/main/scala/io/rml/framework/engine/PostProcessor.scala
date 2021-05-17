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
package io.rml.framework.engine

import io.rml.framework.api.RMLEnvironment
import io.rml.framework.core.model.rdf.SerializableRDFQuad
import io.rml.framework.core.util._
import org.apache.jena.riot.{Lang, RDFDataMgr}

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import scala.collection.immutable.List



/**
  * Processes the generated triples from one record.
  *
  */
trait PostProcessor extends Serializable{

  /**
    * Serializes quads according to PostProcessor
    * @param serializableRDFQuads
    * @return A sequence of pairs (set of logical target IDs, output string)
    */
  def process(serializableRDFQuads: Iterable[SerializableRDFQuad]): Iterable[(String, String)]

  def outputFormat: Format
}

trait AtMostOneProcessor extends PostProcessor  // TODO: define exact semantics of AtMostOneProcessor


/**
  * Reders the RDF statements as a sequence of (logical target ID, N-Quad) pairs.
  */
class NopPostProcessor extends PostProcessor {
  override def process(serializableRDFQuads: Iterable[SerializableRDFQuad]): Iterable[(String, String)] = {
    serializableRDFQuads.flatMap(quad => {
      val quadStr = quad.toString
      for (logicalTargetID <- quad.logicalTargetIDs) yield (logicalTargetID, quadStr)
    })
  }

  override def outputFormat: Format = NQuads
}

/**
  * Groups the list of generated statements from one record into a pair
  * (logical target ID, one big string of N-Quads separated by '\n').
  */
class BulkPostProcessor extends AtMostOneProcessor {
  override def process(serializableRDFQuads: Iterable[SerializableRDFQuad]): Iterable[(String, String)] = {
    // first group quads per logical target ID
    val logicalTargetIDs2outputStrings: Map[String, Set[String]] = Util.groupQuadStringsPerLogicalTargetID(serializableRDFQuads).toMap

    logicalTargetIDs2outputStrings.flatMap(logicalTargetID2outputStrings => {
      val logicalTargetID = logicalTargetID2outputStrings._1
      val outputStrings: String = logicalTargetID2outputStrings._2.mkString("\n")
      List((logicalTargetID, outputStrings))
    })
  }

  override def outputFormat: Format = NQuads
}

/**
  *
  * Formats the generated statements into (logical target ID, json-ld string) pairs
  */
class JsonLDProcessor() extends AtMostOneProcessor {


  override def outputFormat: Format = JSON_LD

  override def process(serializableRDFQuads: Iterable[SerializableRDFQuad]): Iterable[(String, String)] = {
    if (serializableRDFQuads.isEmpty || serializableRDFQuads.mkString.isEmpty) {
      return List()
    }

    val logicalTargetIDs2outputStrings: Map[String, Set[String]] = Util.groupQuadStringsPerLogicalTargetID(serializableRDFQuads).toMap
    logicalTargetIDs2outputStrings.flatMap(logicalTargetID2outputStrings => {
      val logicalTargetID = logicalTargetID2outputStrings._1
      val outputStrings: String = logicalTargetID2outputStrings._2.mkString("\n")
      val dataset = JenaUtil.readDataset(outputStrings, RMLEnvironment.getGeneratorBaseIRI().getOrElse(""), NQuads)
      val bos: ByteArrayOutputStream = new ByteArrayOutputStream
      val jsonLDOutput = Util.tryWith(bos: ByteArrayOutputStream) {
        bos => {
          RDFDataMgr.write(bos, dataset, Lang.JSONLD)
          bos.toString(StandardCharsets.UTF_8.name())
        }
      }
      List((logicalTargetID, jsonLDOutput))
    })
  }
}
