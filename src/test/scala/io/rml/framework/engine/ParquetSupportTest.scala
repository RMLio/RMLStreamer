package io.rml.framework.engine

import io.rml.framework.{FunctionMappingTest, Main}
import io.rml.framework.core.util.Util
import io.rml.framework.util.TestUtil
import io.rml.framework.util.logging.Logger
import org.scalatest.Matchers

import java.io.File

class ParquetSupportTest extends FunctionMappingTest with Matchers {

  private def executeTest(mappingFile: String): Unit = {
    val testDir = Util.getFile(new File(mappingFile).getParent)
    val mappingFileAbs = new File(testDir, new File(mappingFile).getName)

    // read the mapping
    val formattedMapping = Util.readMappingFile(mappingFileAbs.getAbsolutePath)

    print(s"Contains dataset maps? ${formattedMapping.containsDatasetTriplesMaps()}\n")

    // execute the mappping
    val result = Main.createDataSetFromFormattedMapping(formattedMapping).collect()

    val (expectedOutput, expectedOutputFormat) = TestUtil.getExpectedOutputs(testDir)

    val testOutcome = TestUtil.compareResults(s"Parquet test: ${testDir}", result, expectedOutput, postProcessor.outputFormat, expectedOutputFormat)

    testOutcome match {
      case Left(l) =>
        Logger.logError(l);
        fail(l)
      case Right(r) =>
        Logger.logSuccess(r)
    }
  }

  test("Parquet empty") {
    executeTest("parquet/empty_database/mapping.ttl")
  }

  test("Parquet single entry") {
    executeTest("parquet/single_entry/mapping.ttl")
  }

  test("Parquet multiple entries") {
    executeTest("parquet/multiple_entries/mapping.ttl")
  }
}
