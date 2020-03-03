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

package io.rml.framework.std

import org.scalatest.{FunSuite, Matchers}

class StdMappingExtractorTest extends FunSuite with Matchers {

  /**test("testExtract") {

    // ============================================================================================
    // Test info
    // ============================================================================================

    /**
      * Tests the normal flow.
      * Extracts a logical source with a source, reference formulation and an iterator.
      */

    // ============================================================================================
    // Test setup
    // ============================================================================================

    // all RDFResource/RDFLiteral instances will be automatically added here through implicits
    implicit val graph: RDFGraph = RDFGraph(Some(Uri("#RMLMapping")))

    RDFResource("#TripleMap")

      .addProperty(RDFVoc.Property.TYPE,  RMLVoc.Class.TRIPLEMAP)

        .addProperty(RMLVoc.Property.LOGICALSOURCE,
          RDFResource(Uri("#LogicalSource"))
            .addLiteral(RMLVoc.Property.SOURCE, "source.json")
            .addLiteral(RMLVoc.Property.ITERATOR, "$")
            .addProperty(RMLVoc.Property.REFERENCEFORMULATION, RMLVoc.Class.JSONPATH))

        .addProperty(RMLVoc.Property.SUBJECTMAP,
          RDFResource(Uri("#SubjectMap"))
            .addLiteral(RMLVoc.Property.CONSTANT, "#subject"))

    // ============================================================================================
    // Test execution
    // ============================================================================================

    val mapping : RMLMapping = MappingExtractor().extract(graph)

    // ============================================================================================
    // Test verification
    // ============================================================================================

    mapping.uri should be (Uri("#RMLMapping"))
    mapping.triplesMaps.size should be (1)

  }

    **/

}
