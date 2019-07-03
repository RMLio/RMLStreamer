package io.rml.framework.engine

import java.io.{IOException, ObjectInputStream}

import io.rml.framework.core.model.rdf.RDFGraph
import io.rml.framework.core.model.rdf.jena.JenaGraph
import io.rml.framework.core.util.{JSON_LD, JenaUtil, NTriples}

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
class JsonLDProcessor(prefix:String = "", @transient var graph:RDFGraph = JenaGraph()) extends PostProcessor with Serializable {
  override def process(quadStrings: List[String]): List[String] = {
    if (quadStrings.isEmpty || quadStrings.mkString("").isEmpty) {
      return List()
    }
    val quads =  quadStrings.mkString("\n")
    graph.read(quads, JenaUtil.format(NTriples))
    val result = List(graph.write(JSON_LD).trim().replaceAll("\n", " "))
    graph.clear()
    result
  }

  @throws(classOf[IOException])
  private def readObject(in: ObjectInputStream): Unit =  {
    in.defaultReadObject()
    graph = JenaGraph()
  }
}