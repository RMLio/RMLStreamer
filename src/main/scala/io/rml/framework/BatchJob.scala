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
import io.rml.framework.core.model.{FormattedRMLMapping, JoinedTripleMap, TripleMap}
import io.rml.framework.engine.statement.StatementEngine
import io.rml.framework.flink.Source
import io.rml.framework.flink.item.{Item, JoinedItem}
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.scala._
import org.apache.flink.api.scala.hadoop.mapreduce.HadoopInputFormat
import org.apache.flink.core.fs.FileSystem.WriteMode
import org.apache.flink.hadoopcompatibility.scala.HadoopInputs
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapred.{FileInputFormat, JobConf, TextInputFormat}
import org.apache.hadoop.mapreduce.Job
import org.apache.mahout.text.wikipedia.XmlInputFormat

// extend a RichFunction to have access to the RuntimeContext
abstract class Processor[T](engine: StatementEngine[T]) extends RichMapFunction[T, List[String]] {

  override def map(in: T): List[String] = {
    engine.process(in).map(triple => triple.toString)
  }

}

class StdProcessor(engine: StatementEngine[Item]) extends Processor[Item](engine)
class JoinedProcessor(engine: StatementEngine[JoinedItem]) extends Processor[JoinedItem](engine)

object BatchJob {

  def main(args: Array[String]): Unit = {

    // Read mapping file
    //val file = new File("/home/wmaroy/framework/src/main/resources/json_data_mapping.ttl")
    val file = new File("/home/wmaroy/framework/src/main/resources/json_data_mapping.ttl")

    val mapping = MappingReader().read(file)
    val formattedMapping = FormattedRMLMapping.fromRMLMapping(mapping)

    // set up the batch execution environment
    implicit val env = ExecutionEnvironment.getExecutionEnvironment


    // HADOOP TRYOUT

    // TODO: hadoop items need to be parsed to a DataSet[Item]
    //hDataset.print()
    //hDataset.writeAsText("file:///home/wmaroy/framework/output_2.txt", WriteMode.OVERWRITE)
    //"file:///home/wmaroy/github/rml-framework/akka-pipeline/src/main/resources/io/rml/framework/data/orders.xml"
    //"hdfs://localhost:9000/user/wmaroy/orders.xml"))
    // create pipelines for TM with or without PTM
    val dataset = createDataSetFromFormattedMapping(formattedMapping)

    // write dataset to file
    dataset.print()//.writeAsText("file:///home/wmaroy/framework/output.txt", WriteMode.OVERWRITE)

    //standardTMDataset.writeAsText("file:///home/wmaroy/framework/output.txt", WriteMode.OVERWRITE)
    //env.execute("Batch Job")

  }

  private def createDataSetFromFormattedMapping(formattedMapping: FormattedRMLMapping)(implicit env: ExecutionEnvironment): DataSet[String] = {
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

  private def createStandardTripleMapPipeline(triplesMaps: List[TripleMap])(implicit env: ExecutionEnvironment): DataSet[String] = {

    // group triple maps with identical logical sources together
    val grouped = triplesMaps.groupBy(tripleMap => tripleMap.logicalSource)
    // create a map with as key the logical source and as value the engine with loaded statements
    val sourceEngineMap = grouped.map(entry => Source(entry._1) -> {
      println(entry._2.size + " Triple Maps are found.")
      StatementEngine.fromTripleMaps(entry._2)
    })
    val processedDataSets = sourceEngineMap.map(entry => {
      entry._1
        .dataset
        .map(new StdProcessor(entry._2))
        .map(list => if(list.nonEmpty) list.reduce((a, b) => a + "\n" + b) else "")
    })

    unionDataSets(processedDataSets.toList)
  }

  private def createTMWithPTMPipeline(triplesMaps: List[JoinedTripleMap])(implicit env: ExecutionEnvironment): DataSet[String] = {
    // TODO: Check if CoGroup is more efficient than Filter + Join

    val datasets = triplesMaps.map(tm => {
      val engine = StatementEngine.fromJoinedTriplesMap(tm)
      val childDataset = Source(tm.logicalSource)
                            .dataset
                            .filter(item => {
                              item.refer(tm.joinCondition.child.toString).isDefined
                            })
      val parentDataset = Source(tm.parentTriplesMap.logicalSource)
                            .dataset
                            .filter(item => {
                              item.refer(tm.joinCondition.parent.toString).isDefined
                            })

      val joined: JoinDataSet[Item, Item] = childDataset.join(parentDataset)
                                                        .where(_.refer(tm.joinCondition.child.toString).get) // empty fields are already filtered
                                                        .equalTo(_.refer(tm.joinCondition.parent.toString).get) // empty fields are already filtered
      joined.map(items => JoinedItem(items._1, items._2))
            .map(new JoinedProcessor(engine))
            .map(list => if(list.nonEmpty) list.reduce((a, b) => a + "\n" + b) else "")
    })

    unionDataSets(datasets)

  }

  private def unionDataSets[T](datasets: List[DataSet[T]]): DataSet[T] = {
    // error handling for the case where there is no standard TM
    val head = datasets.head
    if(datasets.size > 1) {
      datasets.tail.foldLeft(head)((a, b) => a.union(b))
    } else head
  }

}