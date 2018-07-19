package io.rml.framework.util.fileprocessing

import java.io.File
import java.nio.file.Paths

import io.rml.framework.util.CommentFilter

import scala.io.Source

/**
  * A test helper object to get strings from the expected output turtle file.
  *
  */
object ExpectedOutputTestUtil extends FileProcessingUtil[Set[String]] {
  val filters = Array(new CommentFilter)



  /**
    * Read the given file and return a set of strings (output turtle triples are assumed to be distinct from each other)
    *
    * @param file output turtle file
    * @return set of unique triples in the output turtle file for comparison in the tests
    */
  override def processFile(file: File): Set[String] = {
    var result: Set[String] = Set()

    for (line <- Source.fromFile(file).getLines()) {
      if (filters.map(_.check(line)).forall(p => p)) {
        result += line
      }
    }

    result
  }

  override def candidateFiles: List[String] = List("output.ttl")
}
