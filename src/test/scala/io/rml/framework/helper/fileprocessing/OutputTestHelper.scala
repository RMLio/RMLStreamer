package io.rml.framework.helper.fileprocessing

import java.io.File
import java.nio.file.Paths

import io.rml.framework.helper.CommentFilter

import scala.io.Source

/**
  * A test helper object to get strings from the expected output turtle file.
  *
  */
object OutputTestHelper extends FileProcessingHelper[Set[String]] {
  val filters = Array(new CommentFilter)

  override def getHelperSpecificFiles(path: String): Array[File] = {
      candidateFiles.map( f => Paths.get(path, f).toFile).toArray

  }

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
