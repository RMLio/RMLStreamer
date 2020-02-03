package io.rml.framework.util

import java.io.File
import java.nio.file.Paths

import io.rml.framework.core.internal.Logging
import io.rml.framework.core.util.{Format, JenaUtil}
import io.rml.framework.engine.{BulkPostProcessor, JsonLDProcessor, NopPostProcessor, PostProcessor}
import io.rml.framework.util.logging.Logger
import org.apache.commons.io.FileUtils
import org.apache.jena.rdf.model.Model

object TestProperties {
  def getTempDir(test: String): File = {
    val file = Paths.get(System.getProperty("java.io.tmpdir"), "rml-streamer", test).toFile
    if (!file.exists()) {
      file.mkdir()
    }
    Logger.logInfo(s"Temp folder: ${file.toString}")

    file

  }


}

object TestUtil extends Logging {

  def pickPostProcessor(processorName: String): PostProcessor = {
    processorName match {
      case "bulk" => new BulkPostProcessor
      case "json-ld" => new JsonLDProcessor
      case _ => new NopPostProcessor
    }
  }


  def tmpCleanup(test: String): Unit = {
    val temp = TestProperties.getTempDir(test)
    if (!FileUtils.deleteQuietly(temp)) {
      Logger.logWarning("Could not delete temp dir " + temp.getAbsolutePath)
    }

  }

  def compareResults(testCase: String, expectedOutputs: Set[String], unsanitizedOutput: List[String], format: Format): Either[String,String] = {
    val generatedOutputs = Sanitizer.sanitize(unsanitizedOutput)

    try {
      if (expectedOutputs nonEmpty) {
        val expectedStr = expectedOutputs.mkString("\n")
        val generatedStr = generatedOutputs.mkString("\n")

        Logger.logInfo(List("Generated output: ", generatedStr).mkString("\n"))
        Logger.logInfo(List("Expected Output: ", expectedStr).mkString("\n"))

        compareSets(generatedStr, expectedStr, format)
      }
      Right(s"Testcase ${testCase} passed streaming test!")
    } catch {
      case anyException => {
        val message = s"Testcase ${testCase} FAILED: ${anyException.getMessage}"
        logError(message)
        Left(message)
      }
    }
  }

  private def compareSets(quads1: String, quads2: String, format: Format): Unit = {
    val models1 = quadsToModels(quads1, format)
    val models2 = quadsToModels(quads2, format)
    assert(models1.length == models2.length)

    for (index <- models1.indices) {
      assert(models1(index).isIsomorphicWith(models2(index)))
    }
  }

  private def quadsToModels(quads: String, format: Format): List[Model] = {
    val dataset = JenaUtil.readDataset(quads, "base", format)
    JenaUtil.toModels(dataset)
  }

}
