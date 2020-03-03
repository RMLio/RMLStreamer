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

package io.rml.framework.core.extractors.std

import java.io.File

import io.rml.framework.api.RMLEnvironment
import io.rml.framework.core.extractors.{MappingExtractor, MappingReader}
import io.rml.framework.core.model.rdf.RDFGraph
import io.rml.framework.core.model.{RMLMapping, Uri}
import io.rml.framework.core.util.Turtle
import io.rml.framework.shared.ReadException

/**
  * Standard implementation of the MappingReader trait
  */
class StdMappingReader(mappingExtractor: MappingExtractor) extends MappingReader {

  /**
    * Reads a file and converts it to an RMLMapping.
    *
    * @param file File to be read.
    * @throws ReadException Exception when an error occurred while reading.
    * @return
    */
  @throws(classOf[ReadException])
  override def read(file: File): RMLMapping = {
    val graph: RDFGraph = RDFGraph.fromFile(file, RMLEnvironment.getMappingFileBaseIRI(), Turtle)
    mappingExtractor.extract(graph)
  }

  /**
    * Reads a dump and converts it to an RMLMapping
    *
    * @param dump     Dump to be read.
    * @param graphUri Graph URI of the RMLMapping.
    * @throws ReadException Exception when an error occurred while reading.
    * @return
    */
  @throws(classOf[ReadException])
  override def read(dump: String, graphUri: Uri): RMLMapping = {
    val graph: RDFGraph = RDFGraph(Some(graphUri))
    graph.read(dump, RMLEnvironment.getMappingFileBaseIRI(), Turtle)
    mappingExtractor.extract(graph)
  }

}
