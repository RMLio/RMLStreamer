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
package io.rml.framework.engine

import io.rml.framework.Main
import io.rml.framework.api.RMLEnvironment
import io.rml.framework.core.extractors.TriplesMapsCache
import io.rml.framework.core.util.Util
import io.rml.framework.util.TestUtil
import io.rml.framework.util.logging.Logger
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.streaming.api.scala._
import org.scalatest.{FunSuite, Matchers}

import java.io.File

class StatementEngineTest extends FunSuite with Matchers {

  private def executeTest(mappingFile: String): Unit = {
    TriplesMapsCache.clear();
    RMLEnvironment.setGeneratorBaseIRI(Some("http://example.org/base/"))
    implicit val env = ExecutionEnvironment.getExecutionEnvironment
    implicit val senv = StreamExecutionEnvironment.getExecutionEnvironment
    implicit val postProcessor = new NopPostProcessor()

    val testDir = Util.getFile(new File(mappingFile).getParent)
    val mappingFileAbs = new File(testDir, new File(mappingFile).getName)

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


  /** [STATE @ vr 29 mei 2020 13:24:55 CEST] FAILING
   * java -jar ~/Github/RML/rmlmapper.jar -m example10/mapping.ttl                                                                                                                                 ✔  10090  13:24:14
   * 13:24:26.959 [main] ERROR be.ugent.rml.cli.Main               .main(315) - Expected ':', found '/' [line 1]
   */
  test("example10") {
    pending
    executeTest("example10/mapping.rml.ttl")
  }

  test("example1") {
    executeTest("example1/example.rml.ttl")
  }

  test("example2") {
    executeTest("example2/example.rml.ttl")
  }

  test("example2-bn") {
    executeTest("example2-bn/example.rml.ttl")
  }

  test("example2-lang") {
    executeTest("example2-lang/example.rml.ttl")
  }

  test("example2-object") {
    executeTest("example2-object/example.rml.ttl")
  }

  test("example2-pm") {
    executeTest("example2pm/example.rml.ttl")
  }

  test("example3") {
    pending // TODO: no multiple joins supported yet
    executeTest("example3/example3.rml.ttl")
  }

  test("example4") {
    executeTest("example4/example4_Venue.rml.ttl")
  }

  test("example4b") {
    executeTest("example4b/example4_Venue.rml.ttl")
  }

  test("example6") {
    executeTest("example6/example.rml.ttl")
  }

  ignore("example8") {
    executeTest("example8/simergy.rml.ttl")
  }

  test("csv-special-character-headers") {
    pending //TODO: multiple join conditions are not supported yet
    executeTest("csv-extensive-1/complete.rml.ttl")
  }

}
