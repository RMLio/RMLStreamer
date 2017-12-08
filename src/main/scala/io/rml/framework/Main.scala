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
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema

import scala.collection.immutable

/**
  *
  */
object Main {

  def main(args: Array[String]): Unit = {

    // get parameters
    val parameters = ParameterTool.fromArgs(args)
    val mappingPath = parameters.get("path")

    // Read mapping file
    val formattedMapping = readMappingFile(mappingPath)

    // set up execution environments
    implicit val env = ExecutionEnvironment.getExecutionEnvironment
    implicit val senv = StreamExecutionEnvironment.getExecutionEnvironment

    if(formattedMapping.standardTripleMaps.nonEmpty) {

      println("Dataset Job Found.")
      // execute data set job
      val dataset = createDataSetFromFormattedMapping(formattedMapping)
      // write dataset to file
      dataset.writeAsText("file:///home/wmaroy/framework/output.txt", WriteMode.OVERWRITE)
             .name("Write to output")
      env.execute("DATASET JOB")

    } else if(formattedMapping.streamTripleMaps.nonEmpty) {

      println("Datastream Job found.")
      // execute stream job
      val stream = createStreamFromFormattedMapping(formattedMapping)
      stream.writeToSocket("localhost", 9090, new SimpleStringSchema())
      senv.execute("DATASTREAM JOB")

    }

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
        .map(new StdProcessor(entry._2)).name("Execute statements on items.")
        .flatMap(list => if(list.nonEmpty) Some(list.reduce((a, b) => a + "\n" + b) + "\n\n") else None).name("Reduce to strings.")
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
  private def createDataSetFromFormattedMapping(formattedMapping: FormattedRMLMapping)
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
        .map(new StdProcessor(entry._2)).name("Execute statements on items.")
        .flatMap(list => if(list.nonEmpty) Some(list.reduce((a, b) => a + "\n" + b)) else None).name("Reduce to strings.")
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
                              item.refer(tm.joinCondition.child.toString).isDefined
                            }).name("Read child dataset.")

      val parentDataset = Source(tm.parentTriplesMap.logicalSource).asInstanceOf[FileDataSet]
                            .dataset
                            .filter(item => {
                              item.refer(tm.joinCondition.parent.toString).isDefined
                            }).name("Read parent dataset.")

      val joined: JoinDataSet[Item, Item] = childDataset.join(parentDataset)
                                                        .where(_.refer(tm.joinCondition.child.toString).get) // empty fields are already filtered
                                                        .equalTo(_.refer(tm.joinCondition.parent.toString).get) // empty fields are already filtered

      joined.name("Join child and parent.").map(items => JoinedItem(items._1, items._2))
            .map(new JoinedProcessor(engine)).name("Execute statements.")
            .flatMap(list => if(list.nonEmpty) Some(list.reduce((a, b) => a + "\n" + b)) else None).name("Reduce to strings.")

    })

    unionDataSets(datasets)

  }

  /**
    *
    * @param datasets
    * @tparam T
    * @return
    */
  private def unionDataSets[T](datasets: List[DataSet[T]]): DataSet[T] = {
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
  private def unionStreams[T](streams: Iterable[DataStream[T]]): DataStream[T] = {
    // error handling for the case where there is no standard TM
    val head = streams.head
    if(streams.size > 1) {
      streams.tail.foldLeft(head)((a, b) => a.union(b))
    } else head
  }

  // extend a RichFunction to have access to the RuntimeContext
  abstract class Processor[T](engine: StatementEngine[T]) extends RichMapFunction[T, List[String]] {

    override def map(in: T): List[String] = {
      engine.process(in).map(triple => triple.toString)
    }

  }

  class StdProcessor(engine: StatementEngine[Item]) extends Processor[Item](engine)
  class JoinedProcessor(engine: StatementEngine[JoinedItem]) extends Processor[JoinedItem](engine)

}