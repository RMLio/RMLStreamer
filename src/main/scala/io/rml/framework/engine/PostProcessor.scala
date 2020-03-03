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

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

import io.rml.framework.api.RMLEnvironment
import io.rml.framework.core.util._
import io.rml.framework.flink.sink.FlinkRDFQuad
import org.apache.jena.riot.{Lang, RDFDataMgr}



/**
  * Processes the generated triples from one record.
  *
  */
trait PostProcessor extends Serializable{

  def process(quadStrings: Iterable[FlinkRDFQuad]): List[String]

  def outputFormat: Format
}

trait AtMostOneProcessor extends PostProcessor  // TODO: define exact semantics of AtMostOneProcessor


/**
  * Does nothing, returns the input list of strings
  */
class NopPostProcessor extends PostProcessor {
  override def process(quadStrings: Iterable[FlinkRDFQuad]): List[String] = {
    quadStrings.map(_.toString).toList
  }

  override def outputFormat: Format = NQuads
}

/**
  *
  * Groups the list of generated triples from one record into one big
  * string.
  */
class BulkPostProcessor extends AtMostOneProcessor {
  override def process(quadStrings: Iterable[FlinkRDFQuad]): List[String] = {
    List(quadStrings.mkString("\n"))
  }

  override def outputFormat: Format = NQuads
}

/**
  *
  * Format the generated triples into json-ld format
  */
class JsonLDProcessor() extends AtMostOneProcessor {


  override def outputFormat: Format = JSON_LD

  override def process(quadStrings: Iterable[FlinkRDFQuad]): List[String] = {
    if (quadStrings.isEmpty || quadStrings.mkString.isEmpty) {
      return List()
    }
    
    val quads =  quadStrings.mkString("\n")
    val dataset = JenaUtil.readDataset(quads, RMLEnvironment.getGeneratorBaseIRI().getOrElse(""), NQuads)
    val bos = new ByteArrayOutputStream
    Util.tryWith(bos: ByteArrayOutputStream) {
      bos => {
        RDFDataMgr.write(bos, dataset, Lang.JSONLD)
        List(bos.toString(StandardCharsets.UTF_8.name()))
      }
    }
  }
}
