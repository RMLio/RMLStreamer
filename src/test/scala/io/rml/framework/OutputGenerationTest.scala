package io.rml.framework

import java.io.File
import java.nio.file.Path

import io.rml.framework.util.fileprocessing.{ExpectedOutputTestUtil, TripleGeneratorTestUtil}
import io.rml.framework.util.{Logger, Sanitizer}
import io.rml.framework.shared.{RMLException, TermTypeException}
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Sorting
import scala.util.control.Exception


class OutputGenerationTest extends FlatSpec with Matchers {

  val failing = "negative_test_cases/liter_typecast_fail"
  val passing = "rml-testcases"
  val temp = "temp_ignored_testcases/sourceFmt_specialchar_fail"
  "Output from the generator" should "match the output from ouput.ttl" in {
    ExpectedOutputTestUtil.test(temp, checkGeneratedOutput)
    //checkGeneratedOutput(OutputTestHelper.getFile("example2-object").toString)
  }

  it should "throw TermTypeException if the termType of the subject is a Literal" in {
    
    assertThrows[TermTypeException] {
      ExpectedOutputTestUtil.test(failing, checkForTermTypeException)
      throw new TermTypeException("")
    }
  }


  /**
    * Check for thrown TermTypeException when reading invalid term typed subjects.
    *
    *
    * @param testFolderPath
    */
  def checkForTermTypeException(testFolderPath: String): Unit = {
    val catcher = Exception.catching(classOf[TermTypeException])
    val eitherGenerated = catcher.either(TripleGeneratorTestUtil.processFilesInTestFolder(testFolderPath).flatten)


    if (eitherGenerated.isRight) {
      val generatedOutput = Sanitizer.sanitize(eitherGenerated.right.get)
      Logger.logInfo("Generated output: \n" + generatedOutput.mkString("\n"))
      fail
    }
  }


  /**
    * Check and match the generated output with the expected output
    *
    * @param testFolderPath
    */
  def checkGeneratedOutput(testFolderPath: String): Unit = {
    var expectedOutputs: Set[String] = ExpectedOutputTestUtil.processFilesInTestFolder(testFolderPath).toSet.flatten
    var generatedOutputs: List[String] = TripleGeneratorTestUtil.processFilesInTestFolder(testFolderPath).flatten

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

    for (generatedTriple <- generatedOutputs) {

      val errorMsgMismatch = Array("Generated output does not match expected output",
        "Expected: \n" + expectedOutputs.mkString("\n"),
        "Generated: \n" + generatedOutputs.mkString("\n")).mkString("\n")


      assert(expectedOutputs.contains(generatedTriple), errorMsgMismatch)
    }
  }


}
