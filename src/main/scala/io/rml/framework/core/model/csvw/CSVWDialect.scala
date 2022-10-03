package io.rml.framework.core.model.csvw

import io.rml.framework.core.extractors.ExtractorUtil.extractLiteralFromProperty
import io.rml.framework.core.internal.Logging
import io.rml.framework.core.model.rdf.RDFResource
import io.rml.framework.core.vocabulary.CSVWVoc
import org.apache.commons.csv.CSVFormat

/**
  * Class representing the currently supported CSVW dialect options
  * Default CSV values are used when fields are not specified / not supported
  *
  * Currently supported CSVW options are csvw:delimiter ONLY
  *
  * MIT License
  *
  * Copyright (C) 2017 - 2022 RDF Mapping Language (RML)
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
case class CSVWDialect(var delimiter: String = CSVFormat.DEFAULT.getDelimiterString) extends Logging {

  def setOptions(csvFormatBuilder: CSVFormat.Builder): CSVFormat.Builder = {
    csvFormatBuilder
      .setDelimiter(delimiter)
  }

  def parse(resource: RDFResource): CSVWDialect = {
    logWarning("Only csvw:delimiter is supported, other dialect options will be ignored!")
    this.delimiter = getDelimiter(resource)
    this
  }

  private def getDelimiter(resource: RDFResource): String = {
    extractLiteralFromProperty(resource, CSVWVoc.Property.DELIMITER,
      CSVFormat.DEFAULT.getDelimiterString)
  }
}

