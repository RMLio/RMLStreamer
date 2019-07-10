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
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */


import java.io.File
import java.util.Properties

import io.rml.framework.core.extractors.MappingReader
import io.rml.framework.core.internal.Logging
import io.rml.framework.core.model._
import io.rml.framework.engine.{BulkPostProcessor, JsonLDProcessor, NopPostProcessor, PostProcessor}
import io.rml.framework.engine.statement.StatementEngine
import io.rml.framework.flink.connector.kafka.{FixedPartitioner, KafkaConnectorVersionFactory, PartitionerFormat, RMLPartitioner}
import io.rml.framework.flink.item.{Item, JoinedItem}
import io.rml.framework.flink.source.{EmptyItem, FileDataSet, Source}
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.api.scala._
import org.apache.flink.core.fs.FileSystem.WriteMode
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
//import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaProducer09}

import scala.collection.immutable

/**
  *
  */
object Main extends Logging {

  /**
    * Main method that will be executed as a Flink Job by the Flink Framework.
    *
    * @param args
    */
  def main(args: Array[String]): Unit = {


    val EMPTY_VALUE = "__NO_VALUE_KEY"

    // get parameters
    val parameters = ParameterTool.fromArgs(args)
    val mappingPath = if (parameters.has("path")) parameters.get("path")
    else EMPTY_VALUE
    val outputPath = if (parameters.has("outputPath")) new File(parameters.get("outputPath")).getAbsolutePath // file prefix necessary for Flink API
    else EMPTY_VALUE
    val outputSocket = if (parameters.has("socket")) parameters.get("socket")
    else EMPTY_VALUE

    val kafkaBrokers = if (parameters.has("broker-list")) parameters.get("broker-list")
    else EMPTY_VALUE

    val kafkaTopic = if (parameters.has("topic")) parameters.get("topic")
    else EMPTY_VALUE

    val partitionID = if (parameters.has("partition-id")) parameters.get("partition-id")
    else EMPTY_VALUE

    val partitionFormat: PartitionerFormat = PartitionerFormat.fromString(parameters.get("partition-type"))

    implicit val postProcessor:PostProcessor =
      parameters.get("post-process") match {
        case "bulk" => new BulkPostProcessor
        case "json-ld" => new JsonLDProcessor
        case _ => new NopPostProcessor
      }


    // TODO: do logging correctly
    logInfo("Mapping path: " + mappingPath)
    logInfo("Output path: " + outputPath)
    logInfo("Output socket: " + outputSocket)
    logInfo("Kafka brokers: " +  kafkaBrokers)
    logInfo("Kafka Topic: " +  kafkaTopic)
    logInfo("Post-process: "  + postProcessor.toString)
    logInfo("Kafka Partition: " +  partitionID)


    // Read mapping file and format these, a formatted mapping is a rml mapping that is reorganized optimally.
    // Triple maps are also organized in categories (does it contain streams, does it contain joins, ... )
    val formattedMapping = readMappingFile(mappingPath)

    // set up execution environments, Flink needs these to know how to operate (local, cluster mode, ...)
    implicit val env = ExecutionEnvironment.getExecutionEnvironment
    implicit val senv = StreamExecutionEnvironment.getExecutionEnvironment



    senv.enableCheckpointing(5000, CheckpointingMode.AT_LEAST_ONCE);  // This is what Kafka supports ATM, see https://ci.apache.org/projects/flink/flink-docs-release-1.8/dev/connectors/guarantees.html


    // check if the mapping contains standard dataset mappings
    if (formattedMapping.standardTripleMaps.nonEmpty) {

      logInfo("Dataset Job Found.")

      // create a flink dataset from the formatted mapping
      val dataset: DataSet[String] = createDataSetFromFormattedMapping(formattedMapping)

      // write dataset to file, depending on the given parameters
      dataset.writeAsText("file://" + outputPath, WriteMode.OVERWRITE)
        .name("Write to output")

      // execute data set job
      env.execute("DATASET JOB")

      // check if the mapping contains streamed mappings
    } else if (formattedMapping.streamTripleMaps.nonEmpty) {

      logInfo("Datastream Job found.")

      // create a flink stream from the formatted mapping
      val stream = createStreamFromFormattedMapping(formattedMapping)

      // write to a socket if the parameter is given
      if (outputSocket != EMPTY_VALUE) stream.writeToSocket("localhost", outputSocket.toInt, new SimpleStringSchema())

      else if (kafkaBrokers != EMPTY_VALUE && kafkaTopic != EMPTY_VALUE){
        val optConnectFact = KafkaConnectorVersionFactory(Kafka010)
        val kafkaPartitionerProperties =  new Properties()

        kafkaPartitionerProperties.setProperty(RMLPartitioner.PARTITION_ID_PROPERTY,  partitionID)
        kafkaPartitionerProperties.setProperty(RMLPartitioner.PARTITION_FORMAT_PROPERTY, partitionFormat.string())
        val fact = optConnectFact.get
        fact.applySink[String](kafkaBrokers,kafkaTopic, kafkaPartitionerProperties, new SimpleStringSchema(), stream)
      }
      // write to a file if the parameter is given
      else if (!outputPath.contains(EMPTY_VALUE)) stream.writeAsText(outputPath, WriteMode.OVERWRITE)

      // execute stream job
      senv.execute("DATASTREAM JOB")

    }

  }

  /**
    * Utility method for reading a mapping file and converting it to a formatted RML mapping.
    *
    * @param path
    * @return
    */
  private def readMappingFile(path: String): FormattedRMLMapping = {
    val classLoader = getClass.getClassLoader
    val file_1 = new File(path)
    val mapping = if (file_1.isAbsolute) {
      val file = new File(path)
      MappingReader().read(file)
    } else {
      val file = new File(classLoader.getResource(path).getFile)
      MappingReader().read(file)
    }

    FormattedRMLMapping.fromRMLMapping(mapping)
  }

  /**
    * Utility method for creating a Flink DataStream[String] from a formatted mapping.
    * //TODO currently this does not support any kind of joins
    *
    * @param formattedMapping The mapping file
    * @param env              The execution environment needs to be given implicitly
    * @param senv             The execution environment needs to be given implicitly
    * @return
    */
  def createStreamFromFormattedMapping(formattedMapping: FormattedRMLMapping)
                                      (implicit env: ExecutionEnvironment,
                                       senv: StreamExecutionEnvironment,
                                       postProcessor: PostProcessor): DataStream[String] = {

    // to create a Flink Data Stream there must be triple maps that contain streamed logical sources
    require(formattedMapping.streamTripleMaps.nonEmpty)
    val triplesMaps = formattedMapping.streamTripleMaps

    // group triple maps by logical sources
    val grouped = triplesMaps.groupBy(tripleMap => tripleMap.logicalSource)

    // create a map with as key a Source and as value an Engine with loaded statements
    // the loaded statements are the mappings to execute
    val sourceEngineMap = grouped.map(entry => {
      val logicalSource = entry._1
      val tripleMaps = entry._2
      // This creates a Source from a logical source maps this to an Engine with statements loaded from the triple maps
      Source(logicalSource) -> {
        logInfo(entry._2.size + " Triple Maps are found.")
        StatementEngine.fromTripleMaps(tripleMaps)
      }
    })

    // This is the collection of all data streams that are created by the current mapping
    val processedStreams: immutable.Iterable[DataStream[String]] =
      sourceEngineMap.map(entry => {
        val source = entry._1.asInstanceOf[io.rml.framework.flink.source.Stream]
        val engine = entry._2
        // link the different steps in each pipeline
        source.stream // this will generate a stream of items
          .map(item => item)
          // process every item by a processor with a loaded engine
          .map(new StdProcessor(engine))
          .name("Execute statements on items.")

          // format every list of triples (as strings)
          .flatMap(list => if (list.nonEmpty) Some(list.reduce((a, b) => a + "\n" + b) + "\n\n") else None)
          .name("Reduce to strings.")
      })

    // union all streams to one final stream
    unionStreams(processedStreams)

  }

  /**
    * Utility method for creating a Flink DataSet[String] from a formatted mapping.
    *
    * @param formattedMapping The mapping in formatted form
    * @param env              The execution environment needs to be given implicitly
    * @param senv             The execution environment needs to be given implicitly
    * @return
    */
  def createDataSetFromFormattedMapping(formattedMapping: FormattedRMLMapping)
                                       (implicit env: ExecutionEnvironment,
                                        senv: StreamExecutionEnvironment,
                                        postProcessor: PostProcessor): DataSet[String] = {

    /**
      * check if the mapping has standard triple maps and triple maps with joined triple maps
      * Joined triple maps are created from triple maps that contain parent triple maps. The triple maps are split per
      * parent triple map with unique join conditions. Joined triple maps will contain only one join condition.
      * This makes it easier for setting up pipelines that need the joining of two sources.
      */
    if (formattedMapping.standardTripleMaps.nonEmpty && formattedMapping.joinedTripleMaps.nonEmpty) {

      // create a pipeline from the standard triple maps
      val standardTMDataset = createStandardTripleMapPipeline(formattedMapping.standardTripleMaps)

      // create a pipeline from the triple maps that contain parent triple maps
      val tmWithPTMDataSet = createTMWithPTMPipeline(formattedMapping.joinedTripleMaps)

      // combine the two previous pipeline into one
      standardTMDataset.union(tmWithPTMDataSet)

      // check if the formatted mapping only contains triple maps
    } else if (formattedMapping.standardTripleMaps.nonEmpty) {

      // create a standard pipeline
      createStandardTripleMapPipeline(formattedMapping.standardTripleMaps)

    } else { // the formatted mapping only contains joined triple maps

      // create a joined pipeline
      createTMWithPTMPipeline(formattedMapping.joinedTripleMaps)
    }

  }

  /**
    * Creates a pipeline from standard triple maps.
    *
    * @param triplesMaps Triple maps which are standard.
    * @param env         The execution environment needs to be given implicitly
    * @param senv        The execution environment needs to be given implicitly
    * @return
    */
  private def createStandardTripleMapPipeline(triplesMaps: List[TripleMap])
                                             (implicit env: ExecutionEnvironment,
                                              senv: StreamExecutionEnvironment,
                                              postProcessor: PostProcessor): DataSet[String] = {

    // group triple maps by logical sources
    val grouped = triplesMaps.groupBy(tripleMap => tripleMap.logicalSource)

    // create a map with as key a Source and as value an Engine with loaded statements
    // the loaded statements are the mappings to execute
    val sourceEngineMap = grouped.map(entry => {
      val logicalSource = entry._1
      val tripleMaps = entry._2
      // This creates a Source from a logical source maps this to an Engine with statements loaded from the triple maps
      Source(logicalSource) -> {
        logInfo(entry._2.size + " Triple Maps are found.")
        StatementEngine.fromTripleMaps(tripleMaps)
      }
    })

    // This is the collection of all data streams that are created by the current mapping
    val processedDataSets: immutable.Iterable[DataSet[String]] =
      sourceEngineMap.map(entry => {
        val source = entry._1.asInstanceOf[io.rml.framework.flink.source.FileDataSet]
        val engine = entry._2
        // link the different steps in each pipeline
        source.dataset // this will generate a dataset of items

          // process every item by a processor with a loaded engine
          .map(new StdProcessor(engine))
          .name("Execute statements on items.")

          // format every list of triples (as strings)
          .flatMap(list => if (list.nonEmpty) Some(list.reduce((a, b) => a + "\n" + b) + "\n\n") else None)
          .name("Reduce to strings.")
      })

    unionDataSets(processedDataSets.toList)
  }

  /**
    * Creates a joined pipeline.
    *
    * @param triplesMaps Joined triple maps
    * @param env         The execution environment needs to be given implicitly
    * @param senv        The execution environment needs to be given implicitly
    * @return
    */
  private def createTMWithPTMPipeline(triplesMaps: List[JoinedTripleMap])(implicit env: ExecutionEnvironment, senv: StreamExecutionEnvironment, postProcessor: PostProcessor): DataSet[String] = {
    // TODO: Check if CoGroup is more efficient than Filter + Join

    val datasets = triplesMaps.map(tm => {

      // create the mapping engine with loaded statements from every joined triple map
      val engine = StatementEngine.fromJoinedTriplesMap(tm)

      val childDataset =
      // Create a Source from the childs logical source
        Source(tm.logicalSource).asInstanceOf[FileDataSet]
          .dataset

          // filter out all items that do not contain the childs join condition
          .filter(item => {
          if (tm.joinCondition.isDefined) {
            item.refer(tm.joinCondition.get.child.toString).isDefined
          } else true // if there are no join conditions all items can pass

          // filter out all empty items (some iterators can emit empty items)
        }).filter(item => {
          !item.isInstanceOf[EmptyItem]
        })


      val parentDataset =
      // Create a Source from the parents logical source
        Source(tm.parentTriplesMap.logicalSource).asInstanceOf[FileDataSet]
          .dataset

          // filter out all items that do not contain the parents join condition
          .filter(item => {
          if (tm.joinCondition.isDefined) {
            item.refer(tm.joinCondition.get.parent.toString).isDefined
          } else true // if there are no join conditions all items can pass

          // filter out all empty items
        }).filter(item => {
          !item.isInstanceOf[EmptyItem]
        })

      // if there are join conditions defined join the child dataset and the parent dataset
      if (tm.joinCondition.isDefined) {

        // create the joined dataset
        val joined: JoinDataSet[Item, Item] =
          childDataset.join(parentDataset)
            .where(item => {
              item.refer(tm.joinCondition.get.child.toString).get.head
            }) // empty fields are already filtered
            .equalTo(item => {
            item.refer(tm.joinCondition.get.parent.toString).get.head
          }) // empty fields are already filtered

        joined.name("Join child and parent.")

          // combine the joined item into a JoinedItem
          .map(items => {
          val joined = JoinedItem(items._1, items._2)
          joined
        })

          // process the JoinedItems in an engine
          .map(new JoinedProcessor(engine)).name("Execute statements.")

          // format the list of triples as strings
          .flatMap(list => if (list.nonEmpty) Some(list.reduce((a, b) => a + "\n" + b)) else None)
          .name("Reduce to strings.")

      } else { // if there are no join conditions a cross join will be executed

        // create a crossed data set
        val crossed = childDataset.cross(parentDataset)

        crossed.map(items => JoinedItem(items._1, items._2)) // create a JoinedItem from the crossed items
          .map(new JoinedProcessor(engine)).name("Execute statements.") // process the joined items
          .flatMap(list => if (list.nonEmpty) Some(list.reduce((a, b) => a + "\n" + b)) else None) // format the triples
          .name("Reduce to strings.")
      }


    })

    // union all datasets into one dataset
    unionDataSets(datasets)

  }

  /**
    * Union a list of datasets into one dataset
    *
    * @param datasets
    * @tparam T
    * @return
    */
  def unionDataSets[T](datasets: List[DataSet[T]]): DataSet[T] = {
    // error handling for the case where there is no standard TM
    val head = datasets.head
    if (datasets.size > 1) {
      datasets.tail.foldLeft(head)((a, b) => a.union(b))
    } else head
  }

  /**
    * Union a list of streams into one stream
    *
    * @param streams
    * @tparam T
    * @return
    */
  def unionStreams[T](streams: Iterable[DataStream[T]]): DataStream[T] = {
    // error handling for the case where there is no standard TM
    val head = streams.head
    if (streams.size > 1) {
      streams.tail.foldLeft(head)((a, b) => a.union(b))
    } else head
  }

  // Abstract class for creating a custom processing mapping step in a pipeline.
  // extend a RichFunction to have access to the RuntimeContext
  abstract class Processor[T](engine: StatementEngine[T])(implicit postProcessor: PostProcessor) extends RichMapFunction[T, List[String]] {

    override def map(in: T): List[String] = {
      val quadStrings = engine.process(in)
      postProcessor.process(quadStrings)
    }
  }



  // Custom processing class with normal items
  class StdProcessor(engine: StatementEngine[Item])(implicit postProcessor: PostProcessor) extends Processor[Item](engine)


  // Custom processing class with joined items
  class JoinedProcessor(engine: StatementEngine[JoinedItem])(implicit postProcessor: PostProcessor) extends Processor[JoinedItem](engine)


}