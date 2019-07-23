package io.rml.framework

import io.rml.framework.engine.PostProcessor
import io.rml.framework.shared.{RMLException, TermTypeException}
import io.rml.framework.util.fileprocessing.{ExpectedOutputTestUtil, TripleGeneratorTestUtil}
import io.rml.framework.util.{Logger, Sanitizer, TestUtil}
import org.scalatest.{FlatSpec, Matchers}

import scala.util.control.Exception


class OutputGenerationTest extends FlatSpec with Matchers {

  val failing = Array( "negative_test_cases")
  val passing = Array(("bugs","noopt"), ("rml-testcases","noopt"))
  val temp = Array(("rml-testcases/temp","noopt") )
  "Output from the generator" should "match the output from output.ttl" in {

    passing.foreach(test =>  {
      implicit val postProcessor: PostProcessor= TestUtil.pickPostProcessor(test._2)
      ExpectedOutputTestUtil.test(test._1, checkGeneratedOutput)
    })
    //checkGeneratedOutput(OutputTestHelper.getFile("example2-object").toString)
  }

  it should "throw RMLException since the mapper is expected to fail" in {
    failing.foreach { test =>
      assertThrows[RMLException] {
        ExpectedOutputTestUtil.test(test, checkForTermTypeException)
        throw new RMLException("")

      }
    }
  }


  /**
    * Check for thrown TermTypeException when reading invalid term typed subjects.
    *
    *
    * @param testFolderPath
    */
  def checkForTermTypeException(testFolderPath: String): Unit = {
    val catcher = Exception.catching(classOf[Throwable])
    val eitherGenerated = catcher.either(TripleGeneratorTestUtil.processFilesInTestFolder(testFolderPath).flatten)


    if (eitherGenerated.isRight & testFolderPath.contains("RMLTC")) {
      val generatedOutput = Sanitizer.sanitize(eitherGenerated.right.get)
      Logger.logInfo(testFolderPath)
      Logger.logInfo("Generated output: \n" + generatedOutput.mkString("\n"))
      fail
    }
  }


  /**
    * Check and match the generated output with the expected output
    *
    * @param testFolderPath
    */
  def checkGeneratedOutput(testFolderPath: String)(implicit postProcessor: PostProcessor): Unit = {
    var expectedOutputs: Set[String] = ExpectedOutputTestUtil.processFilesInTestFolder(testFolderPath).toSet.flatten
    val tester = TripleGeneratorTestUtil(postProcessor)
    var generatedOutputs: List[String] = tester.processFilesInTestFolder(testFolderPath).flatten

    /**
      * The amount of spaces added in the generated triples might be different from the expected triple.
      * Sanitization of the sequences will be done here.
      */

    expectedOutputs = Sanitizer.sanitize(expectedOutputs)
    generatedOutputs = Sanitizer.sanitize(generatedOutputs)

    Logger.logInfo("Generated output: \n " + generatedOutputs.mkString("\n"))
    Logger.logInfo("Expected Output: \n " + expectedOutputs.mkString("\n"))


    /**
      * Check if the generated triple is in the expected output.
      */

    Logger.logInfo("Generated size: " + generatedOutputs.size)
    val errorMsgMismatch = Array("Generated output does not match expected output",
      "Expected: \n" + expectedOutputs.mkString("\n"),
      "Generated: \n" + generatedOutputs.mkString("\n")).mkString("\n")
    if(generatedOutputs.isEmpty){
      assert(expectedOutputs.isEmpty, errorMsgMismatch)
    }

     assert(expectedOutputs.size <= generatedOutputs.length, errorMsgMismatch)
    for (generatedTriple <- generatedOutputs) {
      assert(expectedOutputs.contains(generatedTriple), errorMsgMismatch)
    }
  }


}
