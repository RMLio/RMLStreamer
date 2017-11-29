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

import io.rml.framework.core.extractors.DataSourceExtractor
import io.rml.framework.core.model.{DataSource, FileDataSource, Literal, Uri}
import io.rml.framework.core.model.rdf.RDFResource
import io.rml.framework.core.vocabulary.RMLVoc
import io.rml.framework.shared.RMLException

class StdDataSourceExtractor(availableDataSources : Map[Uri, (RDFResource) => DataSource])
  extends DataSourceExtractor {

  /**
    * Extracts a data source from a resource.
    * @param node Resource to extract data source from.
    * @return
    */
  override def extract(node: RDFResource): DataSource = {

    val property = RMLVoc.Property.SOURCE
    val properties = node.listProperties(property)

    if(properties.size != 1) throw new RMLException(node.uri + ": only one data source allowed.")

    properties.head match {
      case literal : Literal => FileDataSource(Uri(literal.toString)) // the literal represents a path uri
      case resource : RDFResource => extractDataSourceFromResource(resource)
    }

  }

  /**
    * Retrieves data source properties from a resource that represents a data source.
    * @param resource Resource that represents a data source.
    * @return
    */
  private def extractDataSourceFromResource(resource: RDFResource) : DataSource = {
    if(!availableDataSources.contains(resource.uri))
      throw new RMLException(resource.uri + ": data source not supported.")

    availableDataSources(resource.uri)(resource)
  }

}