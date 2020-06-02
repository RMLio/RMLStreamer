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
package io.rml.framework

import java.io.File

import io.rml.framework.Main
import io.rml.framework.api.RMLEnvironment
import io.rml.framework.core.extractors.{MappingExtractor, MappingReader}
import io.rml.framework.core.function.TransformationLoader
import io.rml.framework.core.model.TransformationMapping
import io.rml.framework.core.util.Util
import io.rml.framework.engine.NopPostProcessor
import io.rml.framework.util.TestUtil

import io.rml.framework.util.logging.Logger
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.streaming.api.scala._
import org.scalatest.{FunSuite, Matchers}
import io.rml.framework.util.logging.Logger

class SandboxTests extends FunSuite with Matchers  {

  val functionFile = new File(getClass.getClassLoader.getResource("functions.ttl").getFile)


  private def executeTest(mappingFile: String): Unit = {
    RMLEnvironment.setGeneratorBaseIRI(Some("http://example.org/base/"))
    implicit val env = ExecutionEnvironment.getExecutionEnvironment
    implicit val senv = StreamExecutionEnvironment.getExecutionEnvironment
    implicit val postProcessor = new NopPostProcessor()

    val testDir = Util.getFile(new File(mappingFile).getParent)
    val mappingFileAbs = new File(testDir, new File(mappingFile).getName)

    // load functions
    MappingReader(MappingExtractor(TransformationMapping)).read(functionFile)
    // read the mapping
    val formattedMapping = Util.readMappingFile(mappingFileAbs.getAbsolutePath)

    // execute
    val result = Main.createDataSetFromFormattedMapping(formattedMapping).collect()

    // get expected output
    //val testDir = new File(mappingFile).getParentFile.getAbsoluteFile
    val (expectedOutput, expectedOutputFormat) = TestUtil.getExpectedOutputs(testDir)

    val testOutcome = TestUtil.compareResults(s"StatementEngineTest: ${testDir}", result, expectedOutput, postProcessor.outputFormat, expectedOutputFormat)
    testOutcome match {
      case Left(e) => {
        Logger.logError(e)
        System.exit(1)
        fail(e)
      }
      case Right(e) => {
        Logger.logSuccess(e)
      }
    }
  }


  /**
   * RML TESTCASES
   */

  /**
   * [STATE @ ]

   */
  test("sandbox/rml-testcases/RMLTC0011b-CSV") {
    pending
    executeTest("sandbox/rml-testcases/RMLTC0011b-CSV/mapping.ttl")

  }

  /**
   * [STATE @ ]
   *

   */
  test("sandbox/rml-testcases/RMLTC0011b-CSV-small") {
    pending
    executeTest("sandbox/rml-testcases/RMLTC0011b-CSV-small/mapping.ttl")

  }



  /**
   * FNO TEST CASES
   */

  test("sandbox/fno-testcases/RMLFNOTC0001-CSV") {
    pending
    executeTest("sandbox/fno-testcases/RMLFNOTC0001-CSV/mapping.ttl")
  }

  test("sandbox/fno-testcases/RMLFNOTC0001-CSV-explicit") {
    pending
    executeTest("sandbox/fno-testcases/RMLFNOTC0001-CSV/mapping_explicit.ttl")
  }


  /**
   * [STATE @ do 28 mei 2020 14:48:03 CEST] FAILING
   * log. source: students.csv
   * used function: grel:toUpperCaseURL
   *  value parameter: reference to "Name"
   */
  test("sandbox/fno-testcases/RMLFNOTC0004-CSV") {
    pending
    executeTest("sandbox/fno-testcases/RMLFNOTC0004-CSV/mapping.ttl")
  }


  /**
   * [STATE @ do 28 mei 2020 14:48:03 CEST] WORKING
   * log. source: students.csv
   * used function: grel:toUpperCase
   *  value parameter: template using {Name}
   */
  test("sandbox/fno-testcases/RMLFNOTC0008-CSV") {
    pending
    executeTest("sandbox/fno-testcases/RMLFNOTC0008-CSV/mapping.ttl")
  }


  /**
   * [STATE @ ]
   * log. source: students.csv
   * used function: grel:toUpperCase
   *  value parameter: reference to "Name"
   */
  test("sandbox/fno-testcases/RMLFNOTC0011-CSV") {
    pending
    //executeTest("sandbox/fno-testcases/RMLFNOTC0011-CSV/mapping.ttl")
  }



}
