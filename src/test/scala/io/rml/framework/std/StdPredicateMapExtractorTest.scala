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

package io.rml.framework.std

import io.rml.framework.core.extractors.PredicateMapExtractor
import io.rml.framework.core.model.rdf.{RDFGraph, RDFResource}
import io.rml.framework.core.model.{Blank, PredicateMap, Uri}
import io.rml.framework.core.vocabulary.RMLVoc
import org.scalatest.{FunSuite, Matchers}

class StdPredicateMapExtractorTest extends FunSuite with Matchers {

  /**
  test("testExtract") {
    // ============================================================================================
    // Test info
    // ============================================================================================

    /**
      * Tests the normal flow.
      * Extracts predicate maps.
      */

    // ============================================================================================
    // Test setup
    // ============================================================================================

    // all RDFResource/RDFLiteral instances will be automatically added here
    implicit val graph: RDFGraph = RDFGraph(Some(Uri("#RMLMapping")))

    val resource =
      RDFResource("#PredicateObjectMap")
        .addProperty(RMLVoc.Property.PREDICATEMAP,
          RDFResource("#PredicateMap")
          .addProperty(RMLVoc.Property.CONSTANT,
            RDFResource("#Predicate")))

        .addProperty(RMLVoc.Property.PREDICATE,
          RDFResource("#Predicate"))

    // ============================================================================================
    // Test execution
    // ============================================================================================

    val predicateMaps : List[PredicateMap] = PredicateMapExtractor().extract(resource)

    // ============================================================================================
    // Test verification
    // ============================================================================================

    predicateMaps.length shouldNot be (0)

    predicateMaps
      .exists(predicateMap => predicateMap.uri == Blank()) should be (true)

    predicateMaps
      .exists(predicateMap =>
        predicateMap.constant.isDefined
        && predicateMap.uri == Uri("#PredicateMap")) should be (true)
  }

    **/

}
