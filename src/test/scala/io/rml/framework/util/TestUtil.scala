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
    logInfo(s"**** Getting expected output from ${folder.toString}")
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
        /**
         * Log the difference between generated and expected graph
         *
         * Example
         * Given a generated set Gset, and the expected set Eset
         * Gset : {A, B, C, D}
         * Eset: {A, B, E}
         *
         * Eset - Gset = {E}    // expected elements in Eset, that were NOT in Gset
         * Gset - Eset = {C, D} // generated elements in Gset, that were NOT in Eset
         */

        val diffStillExpected = expectedModel.difference(generatedModel)
        val diffWasNotExpected = generatedModel.difference(expectedModel)

        logError(s"Graphs are not isomorphic!" +
          s"\n---- Generated graph: Name: [${generatedModelName}]\n${generatedStr}\n---- Expected graph: Name: [${expectedModelName}]\n${expectedStr}"+
          s"\n---- Difference graphs \n" +
          s"\nThe following difference graph represents what is still missing in the generated graph (i.e. what is still expected):\n${JenaUtil.toString(diffStillExpected)}"+
          s"\nThe following difference graph represents what shouldn't be in the generated graph (i.e. what was not expected):\n${JenaUtil.toString(diffWasNotExpected)}"
        )
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
