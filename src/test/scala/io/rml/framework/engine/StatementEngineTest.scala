package io.rml.framework.engine

import java.io.File

import io.rml.framework.Main
import io.rml.framework.Main.getClass
import io.rml.framework.core.extractors.MappingReader
import io.rml.framework.core.model.{FormattedRMLMapping, Uri}
import io.rml.framework.core.model.rdf.RDFGraph
import io.rml.framework.core.model.rdf.jena.JenaGraph
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.streaming.api.scala._
import org.apache.jena.rdf.model.ModelFactory
import org.scalatest.{FunSuite, Matchers}

class StatementEngineTest extends FunSuite with Matchers {


  test("example10") {

    implicit val env = ExecutionEnvironment.getExecutionEnvironment
    implicit val senv = StreamExecutionEnvironment.getExecutionEnvironment

    // read the mapping
    val formattedMapping = readMapping("example10/mapping.rml.ttl")

    // execute
    val result = Main.createDataSetFromFormattedMapping(formattedMapping).collect().reduce((a, b) => a + "\n" + b)

    // compare the results
    val total = readTriplesFromString(result).size


    total should be (700)

  }


  test("example1") {

    implicit val env = ExecutionEnvironment.getExecutionEnvironment
    implicit val senv = StreamExecutionEnvironment.getExecutionEnvironment

    // read the mapping
    //val formattedMapping = readMapping("example1/example.rml.ttl")
    val formattedMapping = readMapping("example1/example.rml.ttl")

    // execute
    val result = Main.createDataSetFromFormattedMapping(formattedMapping).collect().reduce((a, b) => a + "\n" + b)

    // compare the results
    val triples_1 = readTriplesFromString(result)
    val triples_2 = readTriplesFromFile("example1/example.output.ttl")
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
    val triples_1 = readTriplesFromString(result)
    val triples_2 = readTriplesFromFile("example2/example.output.ttl")
    triples_1 should be (triples_2)

  }

  test("example2-bn") {

    implicit val env = ExecutionEnvironment.getExecutionEnvironment
    implicit val senv = StreamExecutionEnvironment.getExecutionEnvironment

    // read the mapping
    val formattedMapping = readMapping("example2-bn/example.rml.ttl")

    // execute
    val result = Main.createDataSetFromFormattedMapping(formattedMapping).collect().reduce((a, b) => a + "\n" + b)

    // compare the results
    val triples_1 = readTriplesFromString(result)
    val triples_2 = readTriplesFromFile("example2-bn/example.output.ttl")
    triples_1.length should be (triples_2.length)
  }

  test("example2-lang") {
    implicit val env = ExecutionEnvironment.getExecutionEnvironment
    implicit val senv = StreamExecutionEnvironment.getExecutionEnvironment

    // read the mapping
    val formattedMapping = readMapping("example2-lang/example.rml.ttl")

    // execute
    val result = Main.createDataSetFromFormattedMapping(formattedMapping).collect().reduce((a, b) => a + "\n" + b)

    // compare the results
    val triples_1 = readTriplesFromString(result)
    val triples_2 = readTriplesFromFile("example2-lang/example.output.ttl")
    triples_1 should be (triples_2)
  }

  test("example2-object") {
    implicit val env = ExecutionEnvironment.getExecutionEnvironment
    implicit val senv = StreamExecutionEnvironment.getExecutionEnvironment

    // read the mapping
    val formattedMapping = readMapping("example2-object/example.rml.ttl")

    // execute
    val result = Main.createDataSetFromFormattedMapping(formattedMapping).collect().reduce((a, b) => a + "\n" + b)

    // compare the results
    val triples_1 = readTriplesFromString(result)
    val triples_2 = readTriplesFromFile("example2-object/example.output.ttl")
    triples_1 should be (triples_2)
  }

  test("example2-pm") {
    implicit val env = ExecutionEnvironment.getExecutionEnvironment
    implicit val senv = StreamExecutionEnvironment.getExecutionEnvironment

    // read the mapping
    val formattedMapping = readMapping("example2pm/example.rml.ttl")

    // execute
    val result = Main.createDataSetFromFormattedMapping(formattedMapping).collect().reduce((a, b) => a + "\n" + b)

    // compare the results
    val triples_1 = readTriplesFromString(result)
    val triples_2 = readTriplesFromFile("example2pm/example.output.ttl")
    triples_1 should be (triples_2)
  }

  /**
  test("example3") {
    implicit val env = ExecutionEnvironment.getExecutionEnvironment
    implicit val senv = StreamExecutionEnvironment.getExecutionEnvironment

    // read the mapping
    val formattedMapping = readMapping("example3/example3.rml.ttl")

    // execute
    val result = Main.createDataSetFromFormattedMapping(formattedMapping).collect().reduce((a, b) => a + "\n" + b)

    // compare the results
    val triples_1 = readTriplesFromString(result)
    val triples_2 = readTriplesFromFile("example3/example3.output.ttl")
    triples_1 should be (triples_2)
  }
  **/
  test("example4") {
    implicit val env = ExecutionEnvironment.getExecutionEnvironment
    implicit val senv = StreamExecutionEnvironment.getExecutionEnvironment

    // read the mapping
    val formattedMapping = readMapping("example4/example4_Venue.rml.ttl")

    // execute
    val result = Main.createDataSetFromFormattedMapping(formattedMapping).collect().reduce((a, b) => a + "\n" + b)

    // compare the results
    val triples_1 = readTriplesFromString(result)
    val triples_2 = readTriplesFromFile("example4/example4_Venue.output.ttl")
    triples_1 should be (triples_2)
  }

  test("example4b") {
    implicit val env = ExecutionEnvironment.getExecutionEnvironment
    implicit val senv = StreamExecutionEnvironment.getExecutionEnvironment

    // read the mapping
    val formattedMapping = readMapping("example4b/example4_Venue.rml.ttl")

    // execute
    val result = Main.createDataSetFromFormattedMapping(formattedMapping).collect().reduce((a, b) => a + "\n" + b)

    // compare the results
    val triples_1 = readTriplesFromString(result)
    val triples_2 = readTriplesFromFile("example4b/example4_Venue.output.ttl")
    triples_1 should be (triples_2)
  }

  test("example6") {
    implicit val env = ExecutionEnvironment.getExecutionEnvironment
    implicit val senv = StreamExecutionEnvironment.getExecutionEnvironment

    // read the mapping
    val formattedMapping = readMapping("example6/example.rml.ttl")

    // execute
    val result = Main.createDataSetFromFormattedMapping(formattedMapping).collect().reduce((a, b) => a + "\n" + b)

    // compare the results
    val triples_1 = readTriplesFromString(result)
    val triples_2 = readTriplesFromFile("example6/example.output.ttl")
    triples_1 should be (triples_2)
  }

  test("example8") {
    implicit val env = ExecutionEnvironment.getExecutionEnvironment
    implicit val senv = StreamExecutionEnvironment.getExecutionEnvironment

    // read the mapping
    val formattedMapping = readMapping("example8/simergy.rml.ttl")

    // execute
    val result = Main.createDataSetFromFormattedMapping(formattedMapping).collect().reduce((a, b) => a + "\n" + b)

    // compare the results
    val triples_1 = readTriplesFromString(result)
    val triples_2 = readTriplesFromFile("example8/simergy.output.ttl")
    triples_1 should be (triples_2)
  }



  private def readMapping(path:String): FormattedRMLMapping = {
    val classLoader = getClass.getClassLoader
    val file_1 = new File(path)
    val mapping = if(file_1.isAbsolute) {
      val file = new File(path)
      MappingReader().read(file)
    } else {
      val file = new File(classLoader.getResource(path).getFile)
      MappingReader().read(file)
    }

    FormattedRMLMapping.fromRMLMapping(mapping)
  }

  private def readTriplesFromString(dump: String) : Seq[String] = {
    val model_1 = JenaGraph(ModelFactory.createDefaultModel()).withUri(Uri(""))
    model_1.read(dump, "N-TRIPLES")
    model_1.listTriples.map(item => item.toString).sorted
  }

  private def readTriplesFromFile(path: String) : Seq[String] = {
    val model_2 = RDFGraph.fromFile(new File(getAbsolutePath(path)))
    model_2.listTriples.map(item => item.toString).sorted
  }

  private def getAbsolutePath(relativePath:String): String = {
    val classLoader = getClass.getClassLoader
    new File(classLoader.getResource(relativePath).getFile).getAbsolutePath
  }

}