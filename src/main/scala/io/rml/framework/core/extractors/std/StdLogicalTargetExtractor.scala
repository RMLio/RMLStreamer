package io.rml.framework.core.extractors.std

import io.rml.framework.core.extractors.{DataTargetExtractor, ExtractorUtil, LogicalTargetExtractor}
import io.rml.framework.core.internal.Logging
import io.rml.framework.core.model.rdf.RDFResource
import io.rml.framework.core.model.{DataTarget, LogicalTarget, Uri}
import io.rml.framework.core.vocabulary.{FormatVoc, RMLTVoc, RMLVoc}
import io.rml.framework.shared.RMLException

import scala.collection.mutable.ListBuffer

/**
  * MIT License
  *
  * Copyright (C) 2017 - 2021 RDF Mapping Language (RML)
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
  * */
class StdLogicalTargetExtractor(dataTargetExtractor: DataTargetExtractor) extends LogicalTargetExtractor with Logging {
  /**
    * Extract.
    *
    * @param node Node to extract from.
    * @return
    */
  override def extract(node: RDFResource): List[LogicalTarget] = {
    logDebug(s"Extracting logical target: ${node.uri}")

    var result = new ListBuffer[LogicalTarget]

    val properties = node.listProperties(RMLVoc.Property.LOGICALTARGET)
    properties.foreach(logicalTargetResource => {
      logicalTargetResource match {
        case resource: RDFResource => {
          result += extractLogicalTargetProperties(resource)
        }
        case _ => throw new RMLException("Only logical target from resource allowed.")
      }
    })

    result.toList
  }

  private def extractLogicalTargetProperties(resource: RDFResource): LogicalTarget = {
    val compression: Option[Uri] = extractCompression(resource)
    val serialization: Uri = extractSerialization(resource)
    val target: DataTarget = dataTargetExtractor.extract(resource)
    LogicalTarget(target, serialization, compression)
  }

  /**
    * Extracts the compression specification.
    * @param resource The Logical Target resource
    * @return An Uri representing the compression, or <code>None</code> if no compression.
    */
  private def extractCompression(resource: RDFResource): Option[Uri] = {
    val compressionResource = ExtractorUtil.extractResourceFromProperty(resource, RMLTVoc.Property.COMPRESSION)
    if (compressionResource.isDefined) {
      Some(compressionResource.get.uri)
    } else {
      None
    }
  }

  /**
    * Extracts the serialization from the logical target.
    * @param resource The resource representing the logical target
    * @return The URI of the resource representing the serialization. The default is http://www.w3.org/ns/formats/N-Quads
    */
  private def extractSerialization(resource: RDFResource): Uri = {
    val serializationResource = ExtractorUtil.extractResourceFromProperty(resource, RMLTVoc.Property.SERIALIZATION)
    if (serializationResource.isDefined) {
      serializationResource.get.uri
    } else {
      Uri(FormatVoc.Class.NQUADS)
    }
  }
}
