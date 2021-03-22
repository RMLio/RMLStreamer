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

import io.rml.framework.core.extractors.{DataSourceExtractor, LogicalSourceExtractor}
import io.rml.framework.core.internal.Logging
import io.rml.framework.core.model.rdf.RDFResource
import io.rml.framework.core.model.{DataSource, Literal, LogicalSource, Uri}
import io.rml.framework.core.util.Util.DEFAULT_ITERATOR_MAP
import io.rml.framework.core.vocabulary.RMLVoc
import io.rml.framework.shared.RMLException

/**
  * Extractor for extracting a logical source from a resource.
  */
class StdLogicalSourceExtractor(dataSourceExtractor: DataSourceExtractor)
  extends LogicalSourceExtractor with Logging {

  /**
    * Extracts logical source from resource.
    *
    * @param node Resource to extract logical source from.
    * @return
    */
  @throws(classOf[RMLException])
  override def extract(node: RDFResource): LogicalSource = {

    logDebug(node.uri + ": Extracting logical source.")

    val property = RMLVoc.Property.LOGICALSOURCE
    val properties = node.listProperties(property)

    if (properties.size != 1)
      throw new RMLException(node.uri + ": invalid amount of logical sources (amount=" + properties.size + ", should be 1 only).")

    val logicalSourceResource = properties.head
    logicalSourceResource match {
      case resource: RDFResource => extractLogicalSourceProperties(resource)
      case literal: Literal => throw new RMLException(literal.value + ": logical source must be a resource.")
    }

  }

  /**
    * Extracts all properties from a logical source resource.
    *
    * @param resource Resource that represents a logical source.
    * @return An instance of LogicalSource.
    */
  @throws(classOf[RMLException])
  private def extractLogicalSourceProperties(resource: RDFResource): LogicalSource = {

    val source: DataSource = extractDataSource(resource)
    val referenceFormulation: Uri = extractReferenceFormulation(resource)
    val iterator: String = extractIterator(resource, referenceFormulation)

    // debug log, check for performance
    if (isDebugEnabled) {
      logDebug(resource.uri + ": Extracted from logical source" +
        ": iterator -> " + iterator +
        ", source -> " + source +
        ", referenceFormulation -> " + referenceFormulation)
    }

    LogicalSource(referenceFormulation, List(iterator), source)

  }

  /**
    * Extracts iterator from a logical source resource.
    *
    * @param resource Resource that represents a logical source.
    * @return Optionally a literal that represents the iterator.
    */
  @throws(classOf[RMLException])
  private def extractIterator(resource: RDFResource, referenceFormulation: Uri): String = {

    val property = RMLVoc.Property.ITERATOR
    val properties = resource.listProperties(property)

    if (properties.size > 1) throw new RMLException(resource.uri + ": invalid amount of iterators.")
    if (properties.isEmpty) {
      return DEFAULT_ITERATOR_MAP(referenceFormulation.value)
    }

    properties.head match {
      case uri: Uri => throw new RMLException(uri + ": iterator must be a literal.")
      case literal: Literal => literal.value
    }

  }

  /**
    * Extracts data source from a logical source resource.
    *
    * @param resource Resource that represents a logical source.
    * @return An instance of a DataSource
    */
  private def extractDataSource(resource: RDFResource): DataSource = {
    dataSourceExtractor.extract(resource)
  }

  /**
    * Extracts reference formulation from a logical source resource.
    *
    * @param resource Resource that represents a logical source.
    * @return A literal that represents the reference formulation.
    */
  @throws(classOf[RMLException])
  private def extractReferenceFormulation(resource: RDFResource): Uri = {

    val property = RMLVoc.Property.REFERENCEFORMULATION
    val properties = resource.listProperties(property)

    if (properties.size != 1) throw new RMLException(resource.uri + ": exactly one reference formulation allowed.")

    properties.head match {
      case resource: RDFResource => resource.uri
      case literal: Literal => throw new RMLException(literal.toString + ": iterator must be a uri.")
    }
  }
}
