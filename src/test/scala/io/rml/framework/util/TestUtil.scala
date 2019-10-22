package io.rml.framework.util

import java.io.File
import java.nio.file.Paths

import io.rml.framework.engine.{BulkPostProcessor, JsonLDProcessor, NopPostProcessor, PostProcessor}
import io.rml.framework.util.logging.Logger
import org.apache.commons.io.FileUtils

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

object TestUtil {

  def pickPostProcessor(processorName: String): PostProcessor = {
    processorName match {
      case "bulk" => new BulkPostProcessor
      case "json-ld" => new JsonLDProcessor
      case _ => new NopPostProcessor
    }
  }


  def tmpCleanup(test: String): Unit = {
    val temp = TestProperties.getTempDir(test)
    FileUtils.deleteQuietly(temp)
  }

}
