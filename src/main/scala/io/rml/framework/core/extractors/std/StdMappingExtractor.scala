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

package io.rml.framework.core.extractors.std

import io.rml.framework.core.extractors.{MappingExtractor, TriplesMapExtractor}
import io.rml.framework.core.model.RMLMapping
import io.rml.framework.core.model.rdf.RDFGraph

/**
  * Extractor for RMLMappings that injects all dependencies for all
  * sub-extractors.
  */
class StdMappingExtractor extends MappingExtractor[RMLMapping] {

  /**
    * Extract.
    *
    * @param graph Node to extract from.
    * @return
    */
  override def extract(graph: RDFGraph): RMLMapping = {
    val triplesMapsExtractor = TriplesMapExtractor()
    val triplesMaps = triplesMapsExtractor.extract(graph)
    RMLMapping(triplesMaps, graph.uri.toString)
  }

}
