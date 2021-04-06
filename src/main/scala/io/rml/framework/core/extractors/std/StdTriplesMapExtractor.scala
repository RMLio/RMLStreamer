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

import io.rml.framework.core.extractors.{TriplesMapsCache, _}
import io.rml.framework.core.internal.Logging
import io.rml.framework.core.model.rdf.{RDFGraph, RDFResource}
import io.rml.framework.core.model.{TriplesMap, Uri}
import io.rml.framework.core.vocabulary.{R2RMLVoc, RMLVoc}
import io.rml.framework.shared.RMLException

/**
  * Extractor for extracting triple maps from a graph.
  */
object StdTriplesMapExtractor extends TriplesMapExtractor with Logging {


  /**
   * Helper method for inferring whether the given resource is a TriplesMap.
   * A resource can be considered a TriplesMap when any of the following conditions are met
   *  1. (trival case) the resource has the TriplesMap type
   *  2. the resource references the following resources:
   *      - exactly ONE logicalSource, specified by rml:logicalSource.
   *      - exactly ONE subjectMap. It may be specified in two ways
   *          - a) using rr:subjectMap
   *          - b) using constant shortcut property rr:subject
   * @param resource
   * @return [Boolean] indicating whether the resource is a TriplesMap
   */
  private def isTriplesMap(resource : RDFResource) : Boolean = {
    val logicalSourceProperty = RMLVoc.Property.LOGICALSOURCE
    val subjectMapProperty = R2RMLVoc.Property.SUBJECTMAP
    val subjectConstantProperty = R2RMLVoc.Property.SUBJECT

    // trivial case
    val isTriplesMap = resource.getType.equals(Some(Uri(R2RMLVoc.Class.TRIPLESMAP)))

    // property requirements for a triplesmap
    val hasExactlyOneLogicalSource = resource.listProperties(logicalSourceProperty).length==1
    val hasExactlyOneSubjectMap =
      (resource.listProperties(subjectMapProperty) ++ resource.listProperties(subjectConstantProperty)).length==1

    // infer whether the resource is a triples map
    val resourceIsTriplesMap = isTriplesMap|(hasExactlyOneLogicalSource&hasExactlyOneSubjectMap)
    resourceIsTriplesMap
  }
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
    val logicalSourcePropertyUri = Uri(RMLVoc.Property.LOGICALSOURCE)
    val potentialTriplesMapResources = graph.filterProperties(logicalSourcePropertyUri)
    val triplesMapResources = potentialTriplesMapResources.filter(isTriplesMap)

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
    val resourceStr = resource.value;
    // errors can occur during extraction of sub structures
    if (TriplesMapsCache.contains(resourceStr)) {
      TriplesMapsCache.get(resourceStr)
    } else {
      try {

        // create a new triple map by extracting all sub structures
        val triplesMap = TriplesMap(PredicateObjectMapExtractor().extract(resource),
          LogicalSourceExtractor().extract(resource),
          SubjectMapExtractor().extract(resource),
          resource.uri.value,
          GraphMapExtractor().extract(resource)
        )
        val t = TriplesMapsCache.put(resourceStr, triplesMap);
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


}
