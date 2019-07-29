package io.rml.framework.util

import java.io.File
import java.nio.file.Files

import io.rml.framework.engine.{BulkPostProcessor, JsonLDProcessor, NopPostProcessor, PostProcessor}
import org.apache.commons.io.FileUtils


object TestUtil {

  def pickPostProcessor(processorName: String): PostProcessor = {
    processorName match {
      case "bulk" => new BulkPostProcessor
      case "json-ld" => new JsonLDProcessor
      case _ => new NopPostProcessor
    }
  }


  def tmpCleanup():Unit = {
    val temp = new File("/tmp/")
    temp.listFiles().foreach( e => FileUtils.deleteQuietly(e))
  }

}
