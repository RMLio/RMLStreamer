package io.rml.framework.helper.fileprocessing

import java.io.File

import scala.io.Source

object DataSourceTestHelper extends FileProcessingHelper[List[String]] {
  override def candidateFiles: List[String] = List[String]("datasource.xml", "datasource.json")


  override def processFile(file: File): List[String] = {

    var result = List[String]()
    var entry = ""

    for (line <- Source.fromFile(file).getLines()) {

      val trimmed = line.trim
      if (trimmed.length > 0) {
        if (trimmed.charAt(0) == '=') {
          // more than one data source entry detected in the file
          result ::= entry + "\n"
          entry = ""
        } else {
          entry += line.replaceAll(" +"," ")
        }
      }
    }

    //append last entry in data source to the result list
    result ::= entry + "\n\r"

    result


  }
}
