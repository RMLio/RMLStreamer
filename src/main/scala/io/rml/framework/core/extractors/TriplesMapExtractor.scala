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

package io.rml.framework.core.extractors

import io.rml.framework.core.extractors.std.StdTriplesMapExtractor
import io.rml.framework.core.model.TriplesMap
import io.rml.framework.core.model.rdf.RDFResource

trait TriplesMapExtractor extends GraphExtractor[List[TriplesMap]] {

  def extractTriplesMapProperties(resource: RDFResource): Option[TriplesMap]

}

/**
  * Companion
  */
object TriplesMapExtractor {

  /**
    * Creates a TriplesMapExtractor through dependency-injection.
    *
    * @param logicalSourceExtractor      Extractor for logical sources.
    * @param subjectMapExtractor         Extractor for subject maps.
    * @param predicateObjectMapExtractor Extractor for predicate object maps.
    * @return
    */
  def apply(logicalSourceExtractor: LogicalSourceExtractor = LogicalSourceExtractor(),
            subjectMapExtractor: SubjectMapExtractor = SubjectMapExtractor(),
            predicateObjectMapExtractor: PredicateObjectMapExtractor =
            PredicateObjectMapExtractor(), graphMapExtractor: GraphMapExtractor = GraphMapExtractor())

  : TriplesMapExtractor = {

    lazy val extractor = new StdTriplesMapExtractor(
      logicalSourceExtractor,
      subjectMapExtractor,
      graphMapExtractor,
      predicateObjectMapExtractor)
    extractor
  }

}
