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

import java.io.File

import io.rml.framework.core.extractors.{MappingExtractor, MappingReader}
import io.rml.framework.core.internal.Logging
import io.rml.framework.core.model.rdf.RDFGraph
import io.rml.framework.core.model.{RMLMapping, Uri}
import org.apache.commons.io.IOUtils
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

class StdMappingReaderTest extends FunSuite with Matchers
                                            with MockitoSugar
                                            with BeforeAndAfter
                                            with Logging {

/**
  test("testReadFromFile") {

    // ============================================================================================
    // Test info
    // ============================================================================================

    /**
      * Tests the normal flow.
      */

    // ============================================================================================
    // Test setup
    // ============================================================================================

    // Create stream from mapping file
    val file : File = new File(this.getClass.getResource("/mappings/mapping_1.ttl").getFile)

    // Create mock dependency
    val mappingExtractorMock = mock[MappingExtractor]
    when(mappingExtractorMock.extract(any[RDFGraph]))
      .thenReturn(mock[RMLMapping])

    // ============================================================================================
    // Test execution
    // ============================================================================================
    val mapping = MappingReader(
      // inject mock up extractor
      mappingExtractorMock
    )
      .read(file)

    // ============================================================================================
    // Test verification
    // ============================================================================================

    mapping shouldNot be (null)
    verify(mappingExtractorMock, times(1)).extract(any[RDFGraph])

  }

  **//**
  test("testReadFromString") {

    // ============================================================================================
    // Test info
    // ============================================================================================

    /**
      * Tests the normal flow.
      */

    // ============================================================================================
    // Test setup
    // ============================================================================================

    val stream = this.getClass.getResourceAsStream("/mappings/mapping_1.ttl")
    val dump = IOUtils.toString(stream)

    // Create mock dependency
    val mappingExtractorMock = mock[MappingExtractor]
    when(mappingExtractorMock.extract(any[RDFGraph]))
      .thenReturn(mock[RMLMapping])

    // ============================================================================================
    // Test execution
    // ============================================================================================

    val mapping = MappingReader(
      mappingExtractorMock // inject mock up extractor
    ).read(dump, Uri("mapping.ttl"))

    // ============================================================================================
    // Test verification
    // ============================================================================================

    mapping shouldNot be (null)
    verify(mappingExtractorMock, times(1)).extract(any[RDFGraph])

  }

  **/

}
