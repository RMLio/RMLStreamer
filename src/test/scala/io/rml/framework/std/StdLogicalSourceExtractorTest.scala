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

class StdLogicalSourceExtractorTest extends FunSuite with Matchers{

  /**
  test("testExtract") {

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

    // all RDFResource/RDFLiteral instances will be automatically added here
    implicit val graph: RDFGraph = RDFGraph(Some(Uri("#RMLMapping")))

    val resource = RDFResource("#TripleMap")
      .addProperty(RMLVoc.Property.LOGICALSOURCE,
        RDFResource("#LogicalSource")
          .addLiteral(RMLVoc.Property.SOURCE, "source.json")
          .addLiteral(RMLVoc.Property.ITERATOR, "$")
          .addProperty(RMLVoc.Property.REFERENCEFORMULATION, RMLVoc.Class.JSONPATH))

    val logicalSourceExtractor = LogicalSourceExtractor()

    // ============================================================================================
    // Test execution
    // ============================================================================================
    val logicalSource = logicalSourceExtractor.extract(resource)

    // ============================================================================================
    // Test verification
    // ============================================================================================
    logicalSource.uri should be (Uri("#LogicalSource"))
    logicalSource.iterator.isDefined should be (true)
    logicalSource.iterator.get should be (Literal("$"))
    val referenceFormulation = Option(logicalSource.referenceFormulation)
    referenceFormulation.isDefined should be (true)
    referenceFormulation.get should be (Uri(RMLVoc.Class.JSONPATH))
    val source = Option(logicalSource.source)
    source.isDefined should be (true)
    source.get.uri should be (Uri("source.json"))

  }

    **/

}
