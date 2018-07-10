package io.rml.framework

import java.io.File

import io.rml.framework.helper.fileprocessing.{OutputTestHelper, TripleGeneratorTestHelper}
import io.rml.framework.helper.{Logger, Sanitizer}
import org.scalatest.FlatSpec

import scala.util.Sorting


class OutputGenerationTest extends FlatSpec {


  "Output from the generator" should "match the output from ouput.ttl" in {

      checkOutputs("rml-testcases")

  }


  def checkOutputs(rootDir: String): Unit = {
    var checkedTestCases = Array("")
    for (pathString <- OutputTestHelper.getTestCaseFolders(rootDir).map(_.toString)) {


      var expectedOutputs: Set[String] = OutputTestHelper.processFilesInTestFolder(pathString).toSet.flatten
      var generatedOutputs: List[String] = TripleGeneratorTestHelper.processFilesInTestFolder(pathString).flatten

      /**
        * The amount of spaces added in the generated triples might be different from the expected triple.
        * Sanitization of the sequences will be done here.
        */

      Logger.logInfo(generatedOutputs.mkString("\n"))
      expectedOutputs = Sanitizer.sanitize(expectedOutputs)
      generatedOutputs = Sanitizer.sanitize(generatedOutputs)

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


      val errorDifferentTripleAmt = Array("Amount of generated triples is different from the expected amount",
        "expected: " + expectedOutputs.size,
        "generated: " + generatedOutputs.size).mkString("\n")

      assert(expectedOutputs.size == generatedOutputs.size, errorDifferentTripleAmt)

      val testCase = new File(pathString).getName
      checkedTestCases :+= testCase
      Logger.logSuccess("Passed processing: " + testCase)
      Logger.lineBreak()

    }
    Sorting.quickSort(checkedTestCases)
    Logger.logInfo("Processed test cases: " + checkedTestCases.mkString("\n"))
    Logger.lineBreak()
  }


}
