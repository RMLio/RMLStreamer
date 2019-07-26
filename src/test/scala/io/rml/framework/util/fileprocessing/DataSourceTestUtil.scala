package io.rml.framework.util.fileprocessing

import java.io.File

import scala.io.Source

object DataSourceTestUtil extends FileProcessingUtil[List[String]] {
  override def candidateFiles: List[String] = List[String]("datasource.xml", "datasource.json", "datasource.csv")


  override def processFile(file: File): List[String] = {

    var result = List[String]()
    var entry = ""
    //TODO: REFACTOR THIS UGLY QUICKIE WICKIE CODE FIX FOR CSV DATA READING
    val regex = "csv".r

    val csvMatches = regex.findAllMatchIn(file.getName)
    val tail = if (csvMatches.hasNext) "\n\n" else "\n"

    for (line <- Source.fromFile(file).getLines) {

      val trimmed = line.trim
      if (trimmed.length > 0) {
        if (trimmed.charAt(0) == '=') {
          // more than one data source entry detected in the file

          result ::= entry + tail
          entry = ""
        } else {
          var formattedLine = line.replaceAll(" +", " ")
          formattedLine = if (csvMatches.hasNext) formattedLine + "\n" else formattedLine
          entry += formattedLine
        }
      }
    }

    //append last entry in data source to the result list
    result ::= entry + tail

    result


  }
}
