package io.rml.framework.engine

import java.io.{IOException, ObjectInputStream}

import io.rml.framework.api.RMLEnvironment
import io.rml.framework.core.model.rdf.RDFGraph
import io.rml.framework.core.model.rdf.jena.JenaGraph
import io.rml.framework.core.util.{JSON_LD, NTriples}
import io.rml.framework.flink.sink.FlinkRDFQuad



/**
  * Processes the generated triples from one record.
  *
  */
trait PostProcessor extends Serializable{

  def process(quadStrings: Iterable[FlinkRDFQuad]): List[String]
}

trait AtMostOneProcessor extends PostProcessor  // TODO: define exact semantics of AtMostOneProcessor


/**
  * Does nothing, returns the input list of strings
  */
class NopPostProcessor extends PostProcessor {
  override def process(quadStrings: Iterable[FlinkRDFQuad]): List[String] = {
    quadStrings.map(_.toString).toList
  }

}

/**
  *
  * Groups the list of generated triples from one record into one big
  * string.
  */
class BulkPostProcessor extends AtMostOneProcessor {
  override def process(quadStrings: Iterable[FlinkRDFQuad]): List[String] = {
    List(quadStrings.mkString("\n"))
  }
}

/**
  *
  * Format the generated triples into json-ld format
  */
class JsonLDProcessor(prefix:String = "", @transient var graph:RDFGraph = JenaGraph()) extends AtMostOneProcessor {
  override def process(quadStrings: Iterable[FlinkRDFQuad]): List[String] = {
    if (quadStrings.isEmpty || quadStrings.mkString.isEmpty) {
      return List()
    }
    val quads =  quadStrings.mkString("\n")
    graph.read(quads, RMLEnvironment.getGeneratorBaseIRI(), NTriples)
    val result = List(graph.write(JSON_LD))
    graph.clear()
    result
  }

  @throws(classOf[IOException])
  private def readObject(in: ObjectInputStream): Unit =  {
    in.defaultReadObject()
    graph = JenaGraph()
  }
}