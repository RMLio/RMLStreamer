package io.rml.framework.util.fileprocessing

import java.io.File

import io.rml.framework.core.util.{Format, Util}
import io.rml.framework.util.CommentFilter

import scala.collection.mutable.ListBuffer
import scala.io.Source

/**
  * A test helper object to get strings from the expected output turtle file.
  *
  */
object ExpectedOutputTestUtil extends FileProcessingUtil[ (List[String], Option[Format]) ] {
  val filters = Array(new CommentFilter)



  /**
    * Read the given file and return a set of strings (output turtle triples are assumed to be distinct from each other)
    *
    * @param file output turtle file
    * @return set of unique triples in the output turtle file for comparison in the tests
    */
  override def processFile(file: File): (List[String], Option[Format]) = {
    val format = Util.guessFormatFromFileName(file.getName)

    var result: ListBuffer[String] = ListBuffer()

    for (line <- Source.fromFile(file).getLines()) {
      if (filters.map(_.check(line)).forall(p => p)) {
        result += line
      }
    }
    (result.toList, format)
  }

  override def candidateFiles: List[String] = List("output.ttl", "output.nq", "output.json")
}
