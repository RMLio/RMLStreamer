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
import io.rml.framework.core.model._
import io.rml.framework.core.model.rdf.RDFResource
import io.rml.framework.core.vocabulary.{RDFVoc, RMLVoc}
import io.rml.framework.shared.RMLException

class StdDataSourceExtractor
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
    val property = RDFVoc.Property.TYPE
    val properties = resource.listProperties(property)
    if(properties.size != 1) throw new RMLException(resource.uri + ": type must be given.")
    properties.head match {
      case classResource: RDFResource => classResource.uri match {
        case Uri(RMLVoc.Class.TCPSOCKETSTREAM) => extractTCPSocketStream(resource)
        case Uri(RMLVoc.Class.FILESTREAM) => extractFileStream(resource)
      }
      case literal: Literal => throw new RMLException(literal.value + ": type must be a resource.")
    }
  }

  private def extractFileStream(resource: RDFResource) : StreamDataSource = {
    val pathProperties = resource.listProperties(RMLVoc.Property.PATH)
    require(pathProperties.length == 1, "exactly 1 path needed.")
    val path = pathProperties.head match {
      case literal : Literal => literal.value
      case res: RDFResource => throw new RMLException(res.uri + ": must be a literal.")
    }
    FileStream(resource.uri, path)
  }

  private def extractTCPSocketStream(resource: RDFResource) : StreamDataSource = {
    val hostNameProperties = resource.listProperties(RMLVoc.Property.HOSTNAME)
    require(hostNameProperties.length == 1, resource.uri.toString + ": exactly 1 hostname needed.")
    val portProperties = resource.listProperties(RMLVoc.Property.PORT)
    require(portProperties.length == 1, resource.uri.toString + ": exactly 1 port needed.")
    val hostName = hostNameProperties.head match {
      case resource: RDFResource => throw new RMLException(resource.uri + ": hostname must be a literal.")
      case literal: Literal => literal.value
    }
    val port = portProperties.head match {
      case resource: RDFResource => throw new RMLException(resource.uri + ": port must be a literal.")
      case literal: Literal => literal.value
    }
    TCPSocketStream(resource.uri, hostName, port.toInt)
  }
}