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

import io.rml.framework.api.RMLEnvironment
import io.rml.framework.engine.{NopPostProcessor, PostProcessor}
import io.rml.framework.flink.util.FunctionsFlinkUtil
import io.rml.framework.util.TestUtil
import io.rml.framework.util.fileprocessing.TripleGeneratorTestUtil.{env, senv}
import io.rml.framework.util.fileprocessing.{ExpectedOutputTestUtil, TripleGeneratorTestUtil}
import io.rml.framework.util.logging.Logger
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}

import scala.util.control.Exception


class OutputGenerationTest extends StaticTestSpec with ReadMappingBehaviour with BeforeAndAfter with BeforeAndAfterAll {

  override def beforeAll(): Unit = {
    FunctionsFlinkUtil.putFunctionFilesInFlinkCache(env.getJavaEnv, senv.getJavaEnv,
      "functions_grel.ttl",
      "grel_java_mapping.ttl",
      "fno/functions_idlab.ttl",
      "fno/functions_idlab_test_classes_java_mapping.ttl"
    )
  }


  // dev note:
  // Explicit type annotation allows to completely comment out the elements of the failing, passing or temp arrays
  //  without causing compilation failures.
  //  This is useful, for example, if you only want to check the tests that should pass.

  val invalidMappingFile : Array[String] = Array(
    "rml-testcases-invalid-mapping-file"
  )
  val passing : Array[Tuple2[String,String]] =Array(
    ("bugs","noopt"),
    ("rml-testcases","noopt"),
    ("fno-testcases", "noopt")
  )

  val negative : Array[String] = Array(
    "rml-testcases-negative"
  )

  val temp : Array[Tuple2[String,String]] = Array(
    ("rml-testcases/temp","noopt")
  )

  ///////////////////////
  // passing test cases
  ///////////////////////

  passing.foreach { test =>
    test._1 should behave like validMappingFile(test._1)
  }

  "Valid mapping output generation" should "match the output from output.ttl" in {
    passing.foreach(test =>  {
      RMLEnvironment.setGeneratorBaseIRI(Some("http://example.com/base/"))
      implicit val postProcessor: PostProcessor= TestUtil.pickPostProcessor(test._2)
      ExpectedOutputTestUtil.test(test._1, true, checkGeneratedOutput)
    })
  }

  ////////////////////////////////////////////
  // test cases where mapping file is invalid
  ////////////////////////////////////////////

  invalidMappingFile foreach  {
    el  =>
      s"Reading invalid mapping files in $el" should behave like invalidMappingFile(el)
  }

  ////////////////////////////////////////////////////////////////////
  // negative test cases: valid mapping but something else goes wrong
  ////////////////////////////////////////////////////////////////////

  negative.foreach { test =>
    test should behave like validMappingFile(test)
    RMLEnvironment.setGeneratorBaseIRI(Some("http://example.com/base/"))
    implicit val postProcessor: PostProcessor= new NopPostProcessor
    ExpectedOutputTestUtil.test(test, false, checkGeneratedOutput)
  }

  /**
    * Check for thrown TermTypeException when reading invalid term typed subjects.
    *
    *
    * @param testFolderPath
    */
  def checkForTermTypeException(testFolderPath: String): Unit = {

    val catcher = Exception.catching(classOf[Throwable])
    val eitherGenerated = catcher.either(TripleGeneratorTestUtil.processFilesInTestFolder(testFolderPath))



    if (eitherGenerated.isRight & testFolderPath.contains("RMLTC")) {
      val (generatedOutput, format) = eitherGenerated.right.get.head
      Logger.logInfo(testFolderPath)
      Logger.logInfo("Generated output: \n" + generatedOutput.mkString("\n"))
      Logger.logError("Expected an exception, but got none.")
      System.exit(1)
      fail
    }
  }


  /**
    * Check and match the generated output with the expected output
    *
    * @param testFolderPath
    */
  def checkGeneratedOutput(testFolderPath: String, shouldPass: Boolean)(implicit postProcessor: PostProcessor): Unit = {
    Logger.logInfo("checkGeneratedOutput(%s)".format(testFolderPath))
    try {
      val (expectedOutput, expectedOutputFormat) = ExpectedOutputTestUtil.processFilesInTestFolder(testFolderPath).head
      val tester = TripleGeneratorTestUtil(postProcessor)

      var (generatedOutput, generatedOutputFormat) = tester.processFilesInTestFolder(testFolderPath).head
      val outcome = TestUtil.compareResults(testFolderPath, generatedOutput, expectedOutput, generatedOutputFormat, expectedOutputFormat)
      outcome match {
        case Left(e) => {
          if (shouldPass) {
            fail(e)
            System.exit(1)
          }
        }
        case Right(e) => {
          if (!shouldPass) {
            fail(e)
            System.exit(1)
          }
        }
      }
    } catch {
      case e: Throwable => {
        if (shouldPass) {
          fail(e);
          System.exit(1)
        }
      }
    }
  }


}
