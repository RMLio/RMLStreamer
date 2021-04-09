package io.rml.framework.core.extractors.std

import io.rml.framework.core.extractors.{DataTargetExtractor, ExtractorUtil}
import io.rml.framework.core.model.rdf.RDFResource
import io.rml.framework.core.model.{DataTarget, FileDataTarget, Uri}
import io.rml.framework.core.vocabulary.{RDFVoc, RMLTVoc, VoIDVoc}
import io.rml.framework.shared.RMLException

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
class StdDataTargetExtractor extends DataTargetExtractor {
  /**
    * Extract.
    *
    * @param node Node to extract from.
    * @return
    */
  override def extract(node: RDFResource): DataTarget = {
    val targetResource = ExtractorUtil.extractSingleResourceFromProperty(node, RMLTVoc.Property.TARGET)
    val targetType = ExtractorUtil.extractSingleResourceFromProperty(targetResource, RDFVoc.Property.TYPE)
    targetType.uri match {
      case Uri(VoIDVoc.Class.DATASET) => extractFileDataTarget(targetResource)
      case _ => throw new RMLException(s"${targetType} not supported as data target.")
    }
  }

  private def extractFileDataTarget(resource: RDFResource): DataTarget = {
    val path = ExtractorUtil.extractSingleResourceFromProperty(resource, VoIDVoc.Property.DATADUMP)
    FileDataTarget(path.uri)
  }

}
