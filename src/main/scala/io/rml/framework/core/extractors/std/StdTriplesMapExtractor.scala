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

import io.rml.framework.core.extractors._
import io.rml.framework.core.internal.Logging
import io.rml.framework.core.model.rdf.{RDFGraph, RDFResource}
import io.rml.framework.core.model.{TriplesMap, Uri}
import io.rml.framework.core.vocabulary.RMLVoc
import io.rml.framework.shared.RMLException

/**
  * Extractor for extracting triple maps from a graph.
  */
class StdTriplesMapExtractor(logicalSourceExtractor: LogicalSourceExtractor,
                             subjectMapExtractor: SubjectMapExtractor,
                             graphMapExtractor: GraphMapExtractor,
                             predicateObjectMapExtractor: PredicateObjectMapExtractor)

  extends TriplesMapExtractor with Logging {

  /**
    * Extracts a set of triple maps from a graph.
    * If extraction for a triple map fails, this triple map will be skipped.
    *
    * @param graph Graph to extract an Array of triple maps from.
    * @return
    */
  override def extract(graph: RDFGraph): List[TriplesMap] = {

    val triplesMapResources = filterTriplesMaps(graph)

    // iterate over each triple map resource
    triplesMapResources.flatMap {
      resource: RDFResource => extractTriplesMapProperties(resource)
    }

  }

  /**
    *
    * @param graph
    */
  private def filterTriplesMaps(graph: RDFGraph): List[RDFResource] = {

    // filter all triple map resources from the graph
    val typeUri = Uri(RMLVoc.Class.TRIPLESMAP)
    val triplesMapResources = (graph.filterResources(typeUri) ++
      graph.filterProperties(Uri(RMLVoc.Property.LOGICALSOURCE))).distinct


    // debug log, inside check for performance
    if (isDebugEnabled) {
      logDebug(graph.uri + ": Extracting triple maps from graph.")
      logDebug(graph.uri + ": Triple maps found: " +
        triplesMapResources.size + ", " + triplesMapResources)
    }

    triplesMapResources
  }

  /**
    *
    * @param resource
    * @return
    */
  def extractTriplesMapProperties(resource: RDFResource): Option[TriplesMap] = {
    // errors can occur during extraction of sub structures
    try {

      // create a new triple map by extracting all sub structures
      val triplesMap = TriplesMap(predicateObjectMapExtractor.extract(resource),
        logicalSourceExtractor.extract(resource),
        subjectMapExtractor.extract(resource),
        resource.uri.toString,
        graphMapExtractor.extract(resource)
      )
      Some(triplesMap)

    } catch {
      // in case of an error, skip this triple map and log warnings
      case e: RMLException =>
        e.printStackTrace()
        logWarning(e.getMessage)
        logWarning(resource.uri + ": Skipping triple map.")
        throw e
    }
  }


}
