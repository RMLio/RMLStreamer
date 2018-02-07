package io.rml.framework

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.io.File
import io.rml.framework.core.extractors.MappingReader
import io.rml.framework.core.model._
import io.rml.framework.engine.statement.StatementEngine
import io.rml.framework.flink.item.{Item, JoinedItem}
import io.rml.framework.flink.source.{FileDataSet, Source}
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.api.scala._
import org.apache.flink.core.fs.FileSystem.WriteMode
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema

import scala.collection.immutable

/**
  *
  */
object Main {

  def main(args: Array[String]): Unit = {

    val EMPTY_VALUE = "__NO_VALUE_KEY"

    // get parameters
    val parameters = ParameterTool.fromArgs(args)
    val mappingPath = if(parameters.has("path")) parameters.get("path")
                      else EMPTY_VALUE
    val outputPath = if(parameters.has("outputPath")) "file://" + new File(parameters.get("outputPath")).getAbsolutePath // file prefix necessary for Flink API
                     else EMPTY_VALUE
    val outputSocket = if(parameters.has("socket")) parameters.get("socket")
                       else EMPTY_VALUE

    println("Mapping path: " + mappingPath)
    println("Output path: " + outputPath)
    println("Output socket: " + outputSocket)

    // Read mapping file
    val formattedMapping = readMappingFile(mappingPath)

    // set up execution environments
    implicit val env = ExecutionEnvironment.getExecutionEnvironment
    implicit val senv = StreamExecutionEnvironment.getExecutionEnvironment

    if(formattedMapping.standardTripleMaps.nonEmpty) {

      println("Dataset Job Found.")

      val dataset: DataSet[String] = createDataSetFromFormattedMapping(formattedMapping)
      // write dataset to file
      dataset.writeAsText("file://" + outputPath, WriteMode.OVERWRITE)
             .name("Write to output")

      // execute data set job
      env.execute("DATASET JOB")

    } else if(formattedMapping.streamTripleMaps.nonEmpty) {

      println("Datastream Job found.")

      val stream = createStreamFromFormattedMapping(formattedMapping)
      if(outputSocket != EMPTY_VALUE) stream.writeToSocket("localhost", outputSocket.toInt, new SimpleStringSchema())

      else if(!outputPath.contains(EMPTY_VALUE)) stream.writeAsText(outputPath, WriteMode.OVERWRITE)

      // execute stream job
      senv.execute("DATASTREAM JOB")

    }

  }

  /**
    * Testing main method with in memory sinks for asserting output triples.
    * @param args
    */
  def test(args: Array[String]) : List[String] = {

    var output : List[String] = List()

    val EMPTY_VALUE = "__NO_VALUE_KEY"

    // get parameters
    val parameters = ParameterTool.fromArgs(args)
    val mappingPath = if(parameters.has("path")) parameters.get("path")
    else EMPTY_VALUE
    val outputPath = if(parameters.has("outputPath")) "file://" + new File(parameters.get("outputPath")).getAbsolutePath // file prefix necessary for Flink API
    else EMPTY_VALUE
    val outputSocket = if(parameters.has("socket")) parameters.get("socket")
    else EMPTY_VALUE

    println("Mapping path: " + mappingPath)
    println("Output path: " + outputPath)
    println("Output socket: " + outputSocket)

    // Read mapping file
    val formattedMapping = readMappingFile(mappingPath)

    // set up execution environments
    implicit val env = ExecutionEnvironment.getExecutionEnvironment
    implicit val senv = StreamExecutionEnvironment.getExecutionEnvironment

    if(formattedMapping.standardTripleMaps.nonEmpty) {

      println("Dataset Job Found.")

      val dataset: DataSet[String] = createDataSetFromFormattedMapping(formattedMapping)
      // write dataset to file
      output = output ++ dataset.collect()

      // execute data set job
      env.execute("DATASET JOB")

    } else if(formattedMapping.streamTripleMaps.nonEmpty) {

      println("Datastream Job found.")

      val stream = createStreamFromFormattedMapping(formattedMapping)
      if(outputSocket != EMPTY_VALUE) stream.addSink(new SinkFunction[String] {
        println("creating stub sink")
        override def invoke(in: String): Unit = {
          synchronized {
            output = output :+ in
          }
        }
      })

      else if(!outputPath.contains(EMPTY_VALUE)) stream.writeAsText(outputPath, WriteMode.OVERWRITE)

      // execute stream job
      senv.execute("DATASTREAM JOB")

    }

    output

  }

  /**
    *
    * @param path
    * @return
    */
  private def readMappingFile(path: String): FormattedRMLMapping = {
    val file = new File(path)
    val mapping = MappingReader().read(file)
    FormattedRMLMapping.fromRMLMapping(mapping)
  }

  /**
    *
    * @param formattedMapping
    * @param env
    * @param senv
    * @return
    */
  private def createStreamFromFormattedMapping(formattedMapping: FormattedRMLMapping)
                                              (implicit env: ExecutionEnvironment,
                                               senv: StreamExecutionEnvironment): DataStream[String] = {

    require(formattedMapping.streamTripleMaps.nonEmpty)

    val triplesMaps = formattedMapping.streamTripleMaps

    // group triple maps with identical logical sources together
    val grouped = triplesMaps.groupBy(tripleMap => tripleMap.logicalSource)

    // create a map with as key the logical source and as value the engine with loaded statements
    val sourceEngineMap = grouped.map(entry => Source(entry._1) -> {
      println(entry._2.size + " Triple Maps are found.")
      StatementEngine.fromTripleMaps(entry._2)
    })

    val processedStreams: immutable.Iterable[DataStream[String]] = sourceEngineMap.map(entry => {
      entry._1.asInstanceOf[io.rml.framework.flink.source.Stream]
        .stream
        .map(item => {
          item
        })
        .map(new StdProcessor(entry._2)).name("Execute statements on items.")
        .flatMap(list => if(list.nonEmpty) Some(list.reduce((a, b) => a + "\n" + b) + "\n\n") else None)
        .name("Reduce to strings.")
    })

    unionStreams(processedStreams)

  }

  /**
    *
    * @param formattedMapping
    * @param env
    * @param senv
    * @return
    */
  def createDataSetFromFormattedMapping(formattedMapping: FormattedRMLMapping)
                                               (implicit env: ExecutionEnvironment,
                                                senv: StreamExecutionEnvironment): DataSet[String] = {

    if(formattedMapping.standardTripleMaps.nonEmpty && formattedMapping.joinedTripleMaps.nonEmpty) {
      val standardTMDataset = createStandardTripleMapPipeline(formattedMapping.standardTripleMaps)
      val tmWithPTMDataSet = createTMWithPTMPipeline(formattedMapping.joinedTripleMaps)
      standardTMDataset.union(tmWithPTMDataSet)
    } else if(formattedMapping.standardTripleMaps.nonEmpty) {
      createStandardTripleMapPipeline(formattedMapping.standardTripleMaps)
    } else {
      createTMWithPTMPipeline(formattedMapping.joinedTripleMaps)
    }
  }

  /**
    *
    * @param triplesMaps
    * @param env
    * @param senv
    * @return
    */
  private def createStandardTripleMapPipeline(triplesMaps: List[TripleMap])
                                             (implicit env: ExecutionEnvironment,
                                              senv: StreamExecutionEnvironment): DataSet[String] = {

    // group triple maps with identical logical sources together
    val grouped = triplesMaps.groupBy(tripleMap => tripleMap.logicalSource)

    // create a map with as key the logical source and as value the engine with loaded statements
    val sourceEngineMap = grouped.map(entry => Source(entry._1) -> {
      println(entry._2.size + " Triple Maps are found.")
      StatementEngine.fromTripleMaps(entry._2)
    })

    val processedDataSets = sourceEngineMap.map(entry => {
      entry._1.asInstanceOf[FileDataSet]
        .dataset
        .map(new StdProcessor(entry._2))
        .name("Execute statements on items.")
        .flatMap(list => if(list.nonEmpty) Some(list.reduce((a, b) => a + "\n" + b)) else None)
        .name("Reduce to strings.")
    })

    unionDataSets(processedDataSets.toList)
  }

  /**
    *
    * @param triplesMaps
    * @param env
    * @param senv
    * @return
    */
  private def createTMWithPTMPipeline(triplesMaps: List[JoinedTripleMap])(implicit env: ExecutionEnvironment,senv: StreamExecutionEnvironment): DataSet[String] = {
    // TODO: Check if CoGroup is more efficient than Filter + Join

    val datasets = triplesMaps.map(tm => {

      val engine = StatementEngine.fromJoinedTriplesMap(tm)

      val childDataset = Source(tm.logicalSource).asInstanceOf[FileDataSet]
                            .dataset
                            .filter(item => {
                              if(tm.joinCondition.isDefined) {
                                item.refer(tm.joinCondition.get.child.toString).isDefined
                              } else true
                            })
                            .name("Read child dataset.")

      val parentDataset = Source(tm.parentTriplesMap.logicalSource).asInstanceOf[FileDataSet]
                            .dataset
                            .filter(item => {
                              if(tm.joinCondition.isDefined) {
                                item.refer(tm.joinCondition.get.parent.toString).isDefined
                              } else true
                            })
                            .name("Read parent dataset.")

      if(tm.joinCondition.isDefined) {
        val joined: JoinDataSet[Item, Item] =
          childDataset.join(parentDataset)
            .where(_.refer(tm.joinCondition.get.child.toString).get) // empty fields are already filtered
            .equalTo(_.refer(tm.joinCondition.get.parent.toString).get) // empty fields are already filtered

        joined.name("Join child and parent.")
          .map(items => JoinedItem(items._1, items._2))
          .map(new JoinedProcessor(engine)).name("Execute statements.")
          .flatMap(list => if(list.nonEmpty) Some(list.reduce((a, b) => a + "\n" + b)) else None)
          .name("Reduce to strings.")
      } else {
        val crossed = childDataset.cross(parentDataset)
        crossed.map(items => JoinedItem(items._1, items._2))
                .map(new JoinedProcessor(engine)).name("Execute statements.")
                .flatMap(list => if(list.nonEmpty) Some(list.reduce((a, b) => a + "\n" + b)) else None)
                .name("Reduce to strings.")
      }



    })

    unionDataSets(datasets)

  }

  /**
    *
    * @param datasets
    * @tparam T
    * @return
    */
  def unionDataSets[T](datasets: List[DataSet[T]]): DataSet[T] = {
    // error handling for the case where there is no standard TM
    val head = datasets.head
    if(datasets.size > 1) {
      datasets.tail.foldLeft(head)((a, b) => a.union(b))
    } else head
  }

  /**
    *
    * @param streams
    * @tparam T
    * @return
    */
  def unionStreams[T](streams: Iterable[DataStream[T]]): DataStream[T] = {
    // error handling for the case where there is no standard TM
    val head = streams.head
    if(streams.size > 1) {
      streams.tail.foldLeft(head)((a, b) => a.union(b))
    } else head
  }

  // extend a RichFunction to have access to the RuntimeContext
  abstract class Processor[T](engine: StatementEngine[T]) extends RichMapFunction[T, List[String]] {

    override def map(in: T): List[String] = {
      engine.process(in)
            .map(triple => triple.toString)
    }

  }

  class StdProcessor(engine: StatementEngine[Item]) extends Processor[Item](engine)
  class JoinedProcessor(engine: StatementEngine[JoinedItem]) extends Processor[JoinedItem](engine)

}