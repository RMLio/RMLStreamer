package io.rml.framework.helper.fileprocessing

import java.io.File
import java.nio.file.Paths

trait FileProcessingHelper[A] extends TestFilesHelper[A] {
  def candidateFiles: List[String]

  override def getHelperSpecificFiles(testCaseFolder: String): Array[File] = {
    candidateFiles.map(f => Paths.get(testCaseFolder, f).toFile).toArray

  }

}

