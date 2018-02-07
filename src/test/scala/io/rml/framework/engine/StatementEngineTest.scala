package io.rml.framework.engine

import java.io.File

import io.rml.framework.Main
import io.rml.framework.core.extractors.MappingReader
import io.rml.framework.core.model.{FormattedRMLMapping, Uri}
import io.rml.framework.core.model.rdf.RDFGraph
import io.rml.framework.core.model.rdf.jena.JenaGraph
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.streaming.api.scala._
import org.apache.jena.rdf.model.ModelFactory
import org.scalatest.{FunSuite, Matchers}

class StatementEngineTest extends FunSuite with Matchers {

  test("example1") {

    implicit val env = ExecutionEnvironment.getExecutionEnvironment
    implicit val senv = StreamExecutionEnvironment.getExecutionEnvironment

    // read the mapping
    val formattedMapping = readMapping("example1/example.rml.ttl")

    // execute
    val result = Main.createDataSetFromFormattedMapping(formattedMapping).collect().reduce((a, b) => a + "\n" + b)

    // compare the results
    val model_1 = JenaGraph(ModelFactory.createDefaultModel()).withUri(Uri(""))
    model_1.read(result, "N-TRIPLES")
    val triples_1 = model_1.listTriples.map(item => item.toString).sorted
    val model_2 = RDFGraph.fromFile(new File(getAbsolutePath("example1/example.output.ttl")))
    val triples_2 = model_2.listTriples.map(item => item.toString).sorted
    triples_1 should be (triples_2)

  }

  test("example2") {

    implicit val env = ExecutionEnvironment.getExecutionEnvironment
    implicit val senv = StreamExecutionEnvironment.getExecutionEnvironment

    // read the mapping
    val formattedMapping = readMapping("example2/example.rml.ttl")

    // execute
    val result = Main.createDataSetFromFormattedMapping(formattedMapping).collect().reduce((a, b) => a + "\n" + b)

    // compare the results
    val model_1 = JenaGraph(ModelFactory.createDefaultModel()).withUri(Uri(""))
    model_1.read(result, "N-TRIPLES")
    val triples_1 = model_1.listTriples.map(item => item.toString).sorted
    val model_2 = RDFGraph.fromFile(new File(getAbsolutePath("example2/example.output.ttl")))
    val triples_2 = model_2.listTriples.map(item => item.toString).sorted

    triples_1 should be (triples_2)

  }


  private def readMapping(fileName:String): FormattedRMLMapping = {
    val classLoader = getClass.getClassLoader
    val file = new File(classLoader.getResource(fileName).getFile)
    val mapping = MappingReader().read(file)
    FormattedRMLMapping.fromRMLMapping(mapping)
  }

  private def getAbsolutePath(relativePath:String): String = {
    val classLoader = getClass.getClassLoader
    new File(classLoader.getResource(relativePath).getFile).getAbsolutePath
  }

}