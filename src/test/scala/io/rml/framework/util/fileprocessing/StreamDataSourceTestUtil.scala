package io.rml.framework.util.fileprocessing

import java.io.File

import io.rml.framework.util.server.TestData

object StreamDataSourceTestUtil extends FileProcessingUtil[TestData] {

  private def generatePossibleSources: List[String] = {
    val key = List("datasource")
    val extensions = List(".csv", ".xml", ".json")
    val tag = 1 to 10

    for {
      k <- key
      e <- extensions
      t <- tag
      file = k + t + e
    } yield file
  }

  override def candidateFiles: List[String] = generatePossibleSources ++ DataSourceTestUtil.candidateFiles

  override def processFile(file: File): TestData = {

    val input = DataSourceTestUtil.processFile(file)
    TestData(file.getName, input)
  }
}
