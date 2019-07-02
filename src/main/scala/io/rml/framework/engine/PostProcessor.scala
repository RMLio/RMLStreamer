package io.rml.framework.engine

import io.rml.framework.core.model.Uri
import io.rml.framework.core.model.rdf.RDFGraph
import io.rml.framework.core.model.rdf.jena.JenaGraph
import io.rml.framework.core.util.{JSON_LD, JenaUtil, NTriples}
import org.apache.jena.rdf.model.ModelFactory

/**
  * Processes the generated triples from one record.
  *
  */
trait PostProcessor extends Serializable{

  def process(quadStrings: List[String]): List[String]
}

/**
  * Does nothing, returns the input list of strings
  */
class NopPostProcessor extends PostProcessor {
  override def process(quadStrings: List[String]): List[String] = {
    quadStrings
  }

}

/**
  *
  * Groups the list of generated triples from one record into one big
  * string.
  */
class BulkPostProcessor extends PostProcessor {
  override def process(quadStrings: List[String]): List[String] = {
    List(quadStrings.mkString("\n"))
  }
}

/**
  *
  * Format the generated triples into json-ld format
  */
class JsonLDProcessor(prefix:String = "") extends PostProcessor {
  override def process(quadStrings: List[String]): List[String] = {
    val quads =  quadStrings.mkString("\n")
    val graph = JenaGraph()
    graph.read(quads, JenaUtil.format(NTriples))
    val result = List(graph.write(JSON_LD).trim().replaceAll("\n", " "))
    result
  }
}