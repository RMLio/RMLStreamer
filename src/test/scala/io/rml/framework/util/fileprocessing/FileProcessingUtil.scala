package io.rml.framework.util.fileprocessing

import java.io.File
import java.nio.file.Paths

trait FileProcessingUtil[A] extends TestFilesUtil[A] {
  def candidateFiles: List[String]

  override def getHelperSpecificFiles(testCaseFolder: String): Array[File] = {
    candidateFiles.map(f => Paths.get(testCaseFolder, f).toFile).toArray

  }

}

