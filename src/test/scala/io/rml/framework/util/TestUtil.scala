package io.rml.framework.util

import java.io.File
import java.nio.file.Paths

import io.rml.framework.core.internal.Logging
import io.rml.framework.core.util.{Format, JenaUtil, NQuads}
import io.rml.framework.engine.{BulkPostProcessor, JsonLDProcessor, NopPostProcessor, PostProcessor}
import io.rml.framework.util.fileprocessing.ExpectedOutputTestUtil
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

  def getExpectedOutputs(folder: File): (List[String], Option[Format]) = {
    ExpectedOutputTestUtil.processFilesInTestFolder(folder.toString).head
  }

  def compareResults(testCase: String, generatedOutput: Seq[String], expectedOutput: List[String], generatedOutputFormat: Format, expectedOutputFormat: Option[Format]): Either[String, String] = {
    try {
      val expectedStr = expectedOutput.mkString("\n")
      val generatedStr = generatedOutput.mkString("\n")
      if (compareSets(generatedStr, expectedStr, generatedOutputFormat, expectedOutputFormat)) {
        Right(s"Testcase ${testCase} PASSED!")
      } else {
        Left(s"Testcase ${testCase} FAILED! Generated and expected output are not isomorphic")
      }
    } catch {
      case anyException: Throwable => {
        val message = s"Testcase ${testCase} FAILED: ${anyException.getMessage}"
        logError(message)
        Left(message)
      }
    }
  }

  private def compareSets(generatedOutput: String, expectedOutput: String, generatedOutputFormat: Format, expectedOutputFormat: Option[Format]): Boolean = {
    val expOutForm = expectedOutputFormat match {
      case Some(format) => format
      case None => {
        logWarning("Could not detect the format of the expected output. Assuming N-QUADS")
        NQuads
      }
    }
    val generatedModels: List[(Model, String)] = quadsToModels(generatedOutput, generatedOutputFormat)
    val expectedModels: List[(Model, String)] = quadsToModels(expectedOutput, expOutForm)
    if (generatedModels.length != expectedModels.length) {
      logError(s"Different number of graphs. Generated: ${generatedModels.length}; Expected: ${expectedModels.length}")
      logError(s"\n========= Generated =========\n${printModels(generatedModels)}\n========= Expected =========\n${printModels(expectedModels)}")
    }
    assert(generatedModels.length == expectedModels.length, s"Number of generated graphs: ${generatedModels.length}; number of expected graphs: ${expectedModels.length}")

    var result = true

    for (index <- generatedModels.indices) {
      val (generatedModel, generatedModelName) = generatedModels(index)
      val (expectedModel, expectedModelName) = expectedModels(index)
      val generatedStr = JenaUtil.toString(generatedModel)
      val expectedStr = JenaUtil.toString(expectedModel)
      logInfo(s"\n---- Generated graph: Name: [${generatedModelName}]\n${generatedStr}\n---- Expected graph: Name: [${expectedModelName}]\n${expectedStr}")

      if (generatedModel.isIsomorphicWith(expectedModel)) {
        logInfo("Both graphs are isomorphic")
      } else {
        logError("Graphs are not isomorphic!")
        result = false
      }

    }
    result
  }

  private def quadsToModels(quads: String, format: Format): List[(Model, String)] = {
    val dataset = JenaUtil.readDataset(quads, "base", format)
    JenaUtil.toModels(dataset)
  }

  private def printModels(models: List[(Model, String)]): String = {
    var result = new String
    models.foreach(modelName => {
      result += s"Graph name: [${modelName._2}]\n graph: \n ${JenaUtil.toString(modelName._1)}\n"
    })
    result
  }

}
