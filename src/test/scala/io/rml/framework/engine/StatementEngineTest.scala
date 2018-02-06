package io.rml.framework.engine

import java.io.File

import io.rml.framework.Main
import io.rml.framework.Main.StdProcessor
import io.rml.framework.core.extractors.MappingReader
import io.rml.framework.core.model.{FormattedRMLMapping, Uri}
import io.rml.framework.core.model.rdf.RDFGraph
import io.rml.framework.core.model.rdf.jena.JenaGraph
import io.rml.framework.engine.statement.StatementEngine
import io.rml.framework.flink.source.{FileDataSet, Source}
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.streaming.api.scala._
import org.apache.jena.rdf.model.ModelFactory
import org.scalatest.{FunSuite, Matchers}

class StatementEngineTest extends FunSuite with Matchers {

  test("example1") {
    implicit val env = ExecutionEnvironment.getExecutionEnvironment
    val formattedMapping = readMapping("example1/example.rml.ttl")
    // group triple maps with identical logical sources together
    val grouped = formattedMapping.standardTripleMaps.groupBy(tripleMap => tripleMap.logicalSource)
    // create list of datasets per logical source
    val datasets = grouped.map(entry => {
      val logicalSource = entry._1
      val tripleMaps = entry._2
      val xmlDataset = FileDataSet.createXMLWithXPathDataSet(getAbsolutePath("example1/example1.xml"), logicalSource.iterator.get.value)
      val engine = StatementEngine.fromTripleMaps(tripleMaps)
      xmlDataset.dataset.map(new StdProcessor(engine))
        .flatMap(list => if(list.nonEmpty) Some(list.reduce((a, b) => a + "\n" + b)) else None)
    })
    val result = Main.unionDataSets(datasets.toList).collect().reduce((a, b) => a + "\n" + b)

    val model = JenaGraph(ModelFactory.createDefaultModel()).withUri(Uri(""))
    model.read(result, "N-TRIPLES")
    val triples_1 = model.listTriples.map(item => item.toString).sorted

    val model_2 = RDFGraph.fromFile(new File(getAbsolutePath("example1/example.output.ttl")))
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
