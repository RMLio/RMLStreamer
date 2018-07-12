package io.rml.framework.helper.fileprocessing

import java.io.File
import java.nio.file.Paths

import io.rml.framework.core.extractors.MappingReader
import io.rml.framework.core.model.FormattedRMLMapping

/**
  * Test helper to read the mapping file and generate a FormattedRMLMapping
  */

object MappingTestHelper extends FileProcessingHelper[FormattedRMLMapping] {


  override def getHelperSpecificFiles(path: String): Array[File] = {
    candidateFiles.map(f => Paths.get(path, f).toFile).toArray

  }

  override def processFile(file: File): FormattedRMLMapping = {
    val mapping = MappingReader().read(file)
    FormattedRMLMapping.fromRMLMapping(mapping)
  }

  override def candidateFiles: List[String] = List("mapping.ttl", "mapping.rml.ttl", "example.rml.ttl")
}
