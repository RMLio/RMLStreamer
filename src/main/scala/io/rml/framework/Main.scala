package io.rml.framework

/**
 * MIT License
 *
 * Copyright (C) 2017 - 2020 RDF Mapping Language (RML)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 **/


import io.rml.framework.api.{FnOEnvironment, RMLEnvironment}
import io.rml.framework.core.extractors.{JoinConfigMapCache, TriplesMapsCache}
import io.rml.framework.core.function.flink.{FnOEnvironmentLoader, FnOEnvironmentStreamLoader, RichItemIdentityFunction, RichStreamItemIdentityFunction}
import io.rml.framework.core.internal.Logging
import io.rml.framework.core.model._
import io.rml.framework.core.util.{StreamerConfig, Util}
import io.rml.framework.engine._
import io.rml.framework.engine.statement.StatementEngine
import io.rml.framework.flink.connector.kafka.{RMLPartitioner, UniversalKafkaConnectorFactory}
import io.rml.framework.flink.item.{Item, JoinedItem}
import io.rml.framework.flink.source.{EmptyItem, FileDataSet, Source}
import io.rml.framework.flink.util.ParameterUtil
import io.rml.framework.flink.util.ParameterUtil.{OutputSinkOption, PostProcessorOption}
import org.apache.flink.api.common.serialization.{SimpleStringEncoder, SimpleStringSchema}
import org.apache.flink.api.scala._
import org.apache.flink.core.fs.FileSystem.WriteMode
import org.apache.flink.core.fs.Path
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.functions.sink.filesystem.{OutputFileConfig, StreamingFileSink}
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.BasePathBucketAssigner
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.{OnCheckpointRollingPolicy}
import org.apache.flink.streaming.api.scala.{DataStream, OutputTag, StreamExecutionEnvironment}
import org.apache.flink.util.Collector
import java.util.Properties

import io.rml.framework.engine.composers.{CrossJoin, StreamJoinComposer}

import scala.collection.{immutable, mutable}

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

    val configRes = ParameterUtil.processParameters(args)
    if (configRes.isEmpty) {
      return
    }
    val config = configRes.get

    logInfo(config.toString())

    // get parameters
    StreamerConfig.setExecuteLocalParallel(config.localParallel)

    implicit val postProcessor: PostProcessor =
      config.postProcessor match {
        case PostProcessorOption.Bulk => new BulkPostProcessor
        case PostProcessorOption.JsonLD => new JsonLDProcessor
        case _ => new NopPostProcessor
      }

    // determine the base IRI of the RDF to generate
    RMLEnvironment.setGeneratorBaseIRI(config.baseIRI)

    // Read mapping file and format these, a formatted mapping is a rml mapping that is reorganized optimally.
    // Triple maps are also organized in categories (does it contain streams, does it contain joins, ... )
    val formattedMapping = Util.readMappingFile(config.mappingFilePath)


    // Default function config
    // TODO: support adding variable function related files using CLI arguments
    FnOEnvironment.loadDefaultConfiguration()
    FnOEnvironment.intializeFunctionLoader()


    // set up execution environments, Flink needs these to know how to operate (local, cluster mode, ...)
    implicit val env = ExecutionEnvironment.getExecutionEnvironment
    implicit val senv = StreamExecutionEnvironment.getExecutionEnvironment


    if (config.checkpointInterval.isDefined) {
      senv.enableCheckpointing(config.checkpointInterval.get, CheckpointingMode.AT_LEAST_ONCE); // This is what Kafka supports ATM, see https://ci.apache.org/projects/flink/flink-docs-release-1.8/dev/connectors/guarantees.html

      // in order for the StreamingFileSink to work correctly, checkpointing needs to be enabled
    } else if (config.outputSink.equals(OutputSinkOption.File) && formattedMapping.containsStreamTriplesMaps()) {
      senv.enableCheckpointing(30000, CheckpointingMode.AT_LEAST_ONCE);
    }

    if (formattedMapping.containsDatasetTriplesMaps() && !formattedMapping.containsStreamTriplesMaps()) {

      logInfo("Dataset Job Found.")

      // create a flink dataset from the formatted mapping
      val dataset: DataSet[String] = createDataSetFromFormattedMapping(formattedMapping)

      // write dataset to file, depending on the given parameters
      if (config.outputSink.equals(OutputSinkOption.File)) {
        dataset.writeAsText(s"file://${config.outputPath.get}", WriteMode.OVERWRITE)
          .name("Write to output")
      }

      // execute data set job
      env.execute(s"${config.jobName} (DATASET JOB)")

      // check if the mapping contains streamed mappings
    } else if (formattedMapping.containsStreamTriplesMaps()) {
      val stream = if (formattedMapping.containsDatasetTriplesMaps()) {
        logInfo("Mixed dataset and datastream job found.")
        // At this moment, we only support the case that there is a "streaming" triples map that has a "static" parent triples map.
        // Only the subject(s) of the parent triples map will be generated
        createMixedPipelineFromFormattedMapping(formattedMapping)
      } else {
        // create a flink stream from the formatted mapping
        logInfo("Datastream Job found.")
        createDataStreamFromFormattedMapping(formattedMapping)
      }

      // write to a socket if the parameter is given
      if (config.outputSink.equals(OutputSinkOption.Socket)) {
        val parts = config.socket.get.split(':')
        val host = parts(0)
        val port = parts(1).toInt
        stream.writeToSocket(host, port, new SimpleStringSchema())
      }

      else if (config.outputSink.equals(OutputSinkOption.Kafka)) {
        val rmlPartitionProperties = new Properties()
        if (config.partitionId.isDefined) {
          rmlPartitionProperties.setProperty(RMLPartitioner.PARTITION_ID_PROPERTY, config.partitionId.get.toString)
        }
        UniversalKafkaConnectorFactory.applySink[String](config.brokerList.get, rmlPartitionProperties, config.topic.get, stream)
      }
      // write to a file if the parameter is given
      else if (config.outputSink.equals(OutputSinkOption.File)) {
        val parts = config.outputPath.get.split('.')
        val path = parts(0)
        val suffix =
          if (parts.length > 1) {
            "." ++ parts.slice(1, parts.length).mkString(".")
          } else {
            if (config.postProcessor.equals(PostProcessorOption.JsonLD)) ".json" else ".nq"
          }
        val sink: StreamingFileSink[String] = StreamingFileSink
          .forRowFormat(new Path(path), new SimpleStringEncoder[String]("UTF-8"))
          .withBucketAssigner(new BasePathBucketAssigner[String])
          .withRollingPolicy(OnCheckpointRollingPolicy.build())
          .withOutputFileConfig(OutputFileConfig
            .builder()
            .withPartSuffix(suffix)
            .build())
          .build()
        stream.addSink(sink).name("Streaming file sink")
      }
      // discard output if the parameter is given
      else if (config.outputSink.equals(OutputSinkOption.None)) {
        stream.addSink(output => {}).name("No output sink")
      }

      // execute stream job
      senv.execute(s"${config.jobName} (DATASTREAM JOB)")

    }

  }


  def createDataStreamFromFormattedMapping(formattedMapping: FormattedRMLMapping)
                                          (implicit env: ExecutionEnvironment,
                                           senv: StreamExecutionEnvironment,
                                           postProcessor: PostProcessor): DataStream[String] = {

    this.logDebug("createDataStreamFromFormattedMapping(...)")

    /**
     * check if the mapping has standard triple maps and triple maps with joined triple maps
     * Joined triple maps are created from triple maps that contain parent triple maps. The triple maps are split per
     * parent triple map with unique join conditions. Joined triple maps will contain only one join condition.
     * This makes it easier for setting up pipelines that need the joining of two sources.
     */
    if (formattedMapping.standardStreamTriplesMaps.nonEmpty && formattedMapping.joinedStreamTriplesMaps.nonEmpty) {

      // create a pipeline from the standard triple maps
      val standardTMDataStream = createStandardStreamPipeline(formattedMapping.standardStreamTriplesMaps)

      // create a pipeline from the triple maps that contain parent triple maps
       val tmWithPTMDataStream= createStreamTMWithPTMPipeline(formattedMapping.joinedStreamTriplesMaps)

      // combine the two previous pipeline into one
      standardTMDataStream.union(tmWithPTMDataStream)

      // check if the formatted mapping only contains triple maps
    } else if (formattedMapping.standardStreamTriplesMaps.nonEmpty) {

      // create a standard pipeline
      createStandardStreamPipeline(formattedMapping.standardStreamTriplesMaps)
    } else {

      createStreamTMWithPTMPipeline(formattedMapping.joinedStreamTriplesMaps)
    }
  }


  def createStreamTMWithPTMPipeline(triplesMaps: List[JoinedTriplesMap])
                                   (implicit env: ExecutionEnvironment,
                                    senv: StreamExecutionEnvironment,
                                    postProcessor: PostProcessor): DataStream[String] = {

    val datasets = triplesMaps.map(tm => {

      // create the mapping engine with loaded statements from every joined triple map
      val engine = StatementEngine.fromJoinedTriplesMap(tm)

      val childDataStream =
      // Create a Source from the childs logical source
        Source(tm.logicalSource).asInstanceOf[io.rml.framework.flink.source.Stream]
          .stream
          // filter out all items that do not contain the childs join condition
          .filter(iterItems => {
            if (tm.joinCondition.isDefined) {
              iterItems.exists(_.refer(tm.joinCondition.get.child.toString).isDefined)
            } else true // if there are no join conditions all items can pass
            // filter out all empty items (some iterators can emit empty items)
          }).filter(iterItems => {
          iterItems.nonEmpty
        })


      val parentTriplesMap = TriplesMapsCache(tm.parentTriplesMap);
      val parentDataStream =
      // Create a Source from the parents logical source
        Source(parentTriplesMap.logicalSource).asInstanceOf[io.rml.framework.flink.source.Stream]
          .stream

          // filter out all items that do not contain the parents join condition
          .filter(iterItems => {
            if (tm.joinCondition.isDefined) {
              iterItems.exists(_.refer(tm.joinCondition.get.parent.toString).isDefined)
            } else true // if there are no join conditions all items can pass

            // filter out all empty items
          }).filter(iterItems => {
          iterItems.nonEmpty
        })

      //TODO: Be able to choose a specific stream join composer to compose the streaming pipeline
      val joinConfigMap = JoinConfigMapCache.getOrElse(tm.joinConfigMap.get, JoinConfigMap(tm.joinCondition))
      val composer = StreamJoinComposer(childDataStream, parentDataStream, tm, joinConfigMap)
      // if there are join conditions defined join the child dataset and the parent dataset
      val joined = composer.composeStreamJoin()
        // process the JoinedItems in an engine
        joined.map(new JoinedStaticProcessor(engine)).name("Execute mapping statements on joined items")
        // format the list of triples as strings
        .flatMap(list => if (list.nonEmpty) Some(list.reduce((a, b) => a + "\n" + b)) else None)
        .name("Convert triples to strings")




    })

    // union all datasets into one dataset
    unionStreams(datasets)

  }

  /**
   * Utility method for creating a Flink DataStream[String] from a formatted mapping.
   * //TODO currently this does not support any kind of joins
   *
   * @param streamTriplesMaps A list of StreamTriplesMap
   * @param env               The execution environment needs to be given implicitly
   * @param senv              The execution environment needs to be given implicitly
   * @return
   */
  def createStandardStreamPipeline(streamTriplesMaps: List[StreamTriplesMap])
                                  (implicit env: ExecutionEnvironment,
                                   senv: StreamExecutionEnvironment,
                                   postProcessor: PostProcessor): DataStream[String] = {

    // to create a Flink Data Stream there must be triple maps that contain streamed logical sources
    val triplesMaps = streamTriplesMaps

    // group triple maps by logical sourceformattedMapping: FormattedRMLMapping
    val grouped = triplesMaps.groupBy(triplesMap => triplesMap.logicalSource.semanticIdentifier)

    // create a map with as key a Source and as value an Engine with loaded statements
    // the loaded statements are the mappings to execute
    val sourceEngineMap = grouped.map(entry => {
      var logicalSource = entry._2.head.logicalSource
      val triplesMaps = entry._2
      val iterators = triplesMaps.flatMap(tm => tm.logicalSource.iterators).distinct
      logicalSource = LogicalSource(logicalSource.referenceFormulation, iterators, logicalSource.source)
      // This creates a Source from a logical source maps this to an Engine with statements loaded from the triple maps
      Source(logicalSource) -> {
        logInfo(entry._2.size + " Triple Maps are found.")
        StatementEngine.fromTriplesMaps(triplesMaps)
      }
    })

    val preProcessingFunction =
      if (FnOEnvironment.getFunctionLoader.isDefined) {
        val functionLoaderOption = FnOEnvironment.getFunctionLoader
        val jarSources = functionLoaderOption.get.getSources
        val classNames = functionLoaderOption.get.getClassNames
        new FnOEnvironmentStreamLoader(jarSources, classNames)
      } else {
        logInfo("FunctionLoader in RMLEnvironment is NOT DEFINED")
        new RichStreamItemIdentityFunction()
      }

    // This is the collection of all data streams that are created by the current mapping
    val processedStreams: immutable.Iterable[DataStream[String]] =
      sourceEngineMap.map(entry => {
        val source = entry._1.asInstanceOf[io.rml.framework.flink.source.Stream]
        val engine = entry._2
        // link the different steps in each pipeline
        source.stream // this will generate a stream of items
          // process every item by a processor with a loaded engine

          .map(preProcessingFunction)
          .map(new StdStreamProcessor(engine))
          .name("Execute mapping statements on items")

          // format every list of triples (as strings)
          .flatMap(
            list => {
              if (list.nonEmpty) {
                Some(list.reduce((a, b) => a + "\n" + b) + "\n\n")
              } else {
                None
              }
            }
          )
          .name("Convert triples to strings")
      })

    // union all streams to one final stream
    unionStreams(processedStreams)

  }

  private def createMixedPipelineFromFormattedMapping(formattedMapping: FormattedRMLMapping)(implicit env: ExecutionEnvironment, senv: StreamExecutionEnvironment, postProcessor: PostProcessor): DataStream[String] = {
    // we assume a streaming child triples map and a static parent triples map
    require(formattedMapping.containsStreamTriplesMaps() && formattedMapping.containsDatasetTriplesMaps() && formattedMapping.containsParentTriplesMaps)

    val tm2Stream = mutable.HashMap.empty[TriplesMap, DataStream[Iterable[Item]]]
    (formattedMapping.standardStreamTriplesMaps ++ formattedMapping.joinedStreamTriplesMaps)
      // group all mappings by logical source
      .groupBy(_.logicalSource.semanticIdentifier)
      .map(entry => {
        val triplesMaps = entry._2
        val firstSource = triplesMaps.head.logicalSource
        val allIterators = triplesMaps
          .flatMap(triplesMap => triplesMap.logicalSource.iterators)
          .distinct
        // create a new logical source with all iterators
        val logicalSource = LogicalSource(firstSource.referenceFormulation, allIterators, firstSource.source)
        (logicalSource, triplesMaps)
      })
      .foreach(logicalSource2triplesMaps => {
        val triplesMaps = logicalSource2triplesMaps._2
        val outputTags = triplesMaps.map(triplesMap => OutputTag[Iterable[Item]](triplesMap.hashCode().toHexString))

        // Dispatch the stream to every triples map by
        // creating a side output stream for every triples map (i.e. split the stream)
        val logicalSource = logicalSource2triplesMaps._1
        val dataStream = Source(logicalSource).asInstanceOf[io.rml.framework.flink.source.Stream].stream
          .process(new ProcessFunction[Iterable[Item], Iterable[Item]] {
            override def processElement(items: Iterable[Item], context: ProcessFunction[Iterable[Item], Iterable[Item]]#Context, out: Collector[Iterable[Item]]): Unit = {
              // we don't need to write the items to 'out' because 
              outputTags.foreach(context.output(_, items))
            }
          })
        triplesMaps.foreach(triplesMap => {
          val outputTag = OutputTag[Iterable[Item]](triplesMap.hashCode().toHexString)
          val streamForTriplesMap = dataStream.getSideOutput(outputTag)
          tm2Stream.put(triplesMap, streamForTriplesMap)
        })
      })

    // map: (parent triples map identifier, name of the variable to join on, the value of it) => generated subject string
    // this map contains the subjects from the static data source in order to perform the join.
    val parentTriplesMapId2JoinParentSource2JoinParentValue2ParentItem = getStaticParentSourceItems(formattedMapping)

    val processedDataStreams: Iterable[DataStream[String]] = tm2Stream.map(entry => {
      val triplesMap = entry._1
      val stream = entry._2

      if (triplesMap.containsParentTriplesMap) {
        // the join scenario
        // create the engine that will process the joined items
        val joinedStreamTm = triplesMap.asInstanceOf[JoinedTriplesMap]
        val engine = StatementEngine.fromJoinedTriplesMap(joinedStreamTm)

        val parentTmId = joinedStreamTm.parentTriplesMap
        val joinParentSource = joinedStreamTm.joinCondition.get.parent.identifier

        stream
          .flatMap(_.iterator)
          .map(childItem => {
            val childRef = childItem.refer(joinedStreamTm.joinCondition.get.child.identifier).get.head
            // for every child ref from the streaming data, we look up the subject item from the static data (saved before in a map)
            val parentItem = parentTriplesMapId2JoinParentSource2JoinParentValue2ParentItem.getOrElse((parentTmId, joinParentSource, childRef), null)
            if (parentItem != null) {
              Some(JoinedItem(childItem, parentItem))
            } else {
              None
            }

          }).filter(_.isDefined) // filter out the None's
          .name("Joining items from static data and streaming data")
          .map(someItem => someItem.get)
          .map(new JoinedStaticProcessor(engine))
          .name("Executing mapping statements on joined items")
          .flatMap(triples =>
            if (triples.nonEmpty) {
              Some(triples.head + "\n\n")
            } else {
              None
            }
          )
      } else {

        // the "normal" scenario.
        val engine = StatementEngine.fromTriplesMaps(List(triplesMap))
        stream
          .map(new StdStreamProcessor(engine))
          .name("Execute mapping statements on items")

          // format every list of triples (as strings)
          .flatMap(list =>
            if (list.nonEmpty) {
              Some(list.reduce((a, b) => a + "\n" + b) + "\n\n")
            } else None
          )
          .name("Convert triples to strings")
      }

    })

    unionStreams(processedDataStreams)
  }

  private def getStaticParentSourceItems(formattedMapping: FormattedRMLMapping)(implicit env: ExecutionEnvironment, senv: StreamExecutionEnvironment, postProcessor: PostProcessor)
  : Map[(String, String, String), Item] = {
    // map: (parent triples map identiefier, name of the variable to join on, the value of it) => generated subject string
    var parentTriplesMapId2JoinParentSource2JoinParentValue2ParentItem = mutable.HashMap.empty[(String, String, String), Item]

    formattedMapping.joinedStreamTriplesMaps.foreach(joinedTm => {
      // identify the parent triples map
      val parentTm = TriplesMapsCache.get(joinedTm.parentTriplesMap).get;

      // find the parent source of the join condition
      val joinParentSource = joinedTm.joinCondition.get.parent.identifier

      // get the subjects from the static logical source
      val parentDataset = Source(parentTm.logicalSource).asInstanceOf[FileDataSet].dataset

      parentDataset
        // only keep items where the parent source of the join condition is defined
        .filter(item => {
          item.refer(joinParentSource).isDefined
        })
        // get the value of the join parent source and generate the subject
        .map(item => {
          // get the value of the parent source
          val joinParentValue = item.refer(joinParentSource).get.head
          //val subject = generated.map(tripleStr => tripleStr.substring(0, tripleStr.indexOf(' '))).head // should only be one subjefct!
          (joinParentSource, joinParentValue, item)
        })
        // now put everything in the map
        .collect()
        .iterator.foreach(tuple => {
        val parentTriplesMap2JoinId2JoinValue = (parentTm.identifier, tuple._1, tuple._2)
        parentTriplesMapId2JoinParentSource2JoinParentValue2ParentItem.put(parentTriplesMap2JoinId2JoinValue, tuple._3)
      })
    })

    parentTriplesMapId2JoinParentSource2JoinParentValue2ParentItem.toMap
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

    this.logDebug("createDataSetFromFormattedMapping(...)")
    require(!postProcessor.isInstanceOf[AtMostOneProcessor], "Bulk output and JSON-LD output are not supported in the static version")

    /**
     * check if the mapping has standard triple maps and triple maps with joined triple maps
     * Joined triple maps are created from triple maps that contain parent triple maps. The triple maps are split per
     * parent triple map with unique join conditions. Joined triple maps will contain only one join condition.
     * This makes it easier for setting up pipelines that need the joining of two sources.
     */
    if (formattedMapping.standardStaticTriplesMaps.nonEmpty && formattedMapping.joinedStaticTriplesMaps.nonEmpty) {

      // create a pipeline from the standard triple maps
      val standardTMDataset = createStandardTriplesMapPipeline(formattedMapping.standardStaticTriplesMaps)

      // create a pipeline from the triple maps that contain parent triple maps
      val tmWithPTMDataSet = createTMWithPTMPipeline(formattedMapping.joinedStaticTriplesMaps)

      // combine the two previous pipeline into one
      standardTMDataset.union(tmWithPTMDataSet)

      // check if the formatted mapping only contains triple maps
    } else if (formattedMapping.standardStaticTriplesMaps.nonEmpty) {

      // create a standard pipeline
      createStandardTriplesMapPipeline(formattedMapping.standardStaticTriplesMaps)

    } else { // the formatted mapping only contains joined triple maps

      // create a joined pipeline
      createTMWithPTMPipeline(formattedMapping.joinedStaticTriplesMaps)
    }

  }


  /**
   * Creates a pipeline from standard triple maps.
   *
   * @param standardTriplesMaps Triple maps which are standard.
   * @param env                 The execution environment needs to be given implicitly
   * @param senv                The execution environment needs to be given implicitly
   * @return
   */
  private def createStandardTriplesMapPipeline(standardTriplesMaps: List[TriplesMap])
                                              (implicit env: ExecutionEnvironment,
                                               senv: StreamExecutionEnvironment,
                                               postProcessor: PostProcessor): DataSet[String] = {
    this.logDebug("createStandardTriplesMapPipeline(standard triples maps..)")
    // group triple maps by logical sources
    val grouped = standardTriplesMaps.groupBy(triplesMap => triplesMap.logicalSource)

    // create a map with as key a Source and as value an Engine with loaded statements
    // the loaded statements are the mappings to execute
    val sourceEngineMap = grouped.map(entry => {
      val logicalSource = entry._1
      val triplesMaps = entry._2
      // This creates a Source from a logical source maps this to an Engine with statements loaded from the triple maps
      Source(logicalSource) -> {
        logInfo(entry._2.size + " Triple Maps are found.")
        StatementEngine.fromTriplesMaps(triplesMaps)
      }
    })

    val preProcessingFunction =
      if (FnOEnvironment.getFunctionLoader.isDefined) {
        val functionLoaderOption = FnOEnvironment.getFunctionLoader
        val jarSources = functionLoaderOption.get.getSources
        val classNames = functionLoaderOption.get.getClassNames
        new FnOEnvironmentLoader(jarSources, classNames)
      } else {
        logInfo("FunctionLoader in RMLEnvironment is NOT DEFINED")
        new RichItemIdentityFunction()
      }

    // This is the collection of all data streams that are created by the current mapping
    val processedDataSets: immutable.Iterable[DataSet[String]] =
      sourceEngineMap.map(entry => {
        val source = entry._1.asInstanceOf[io.rml.framework.flink.source.FileDataSet]
        val engine = entry._2
        // link the different steps in each pipeline
        source.dataset // this will generate a dataset of items

          // process every item by a processor with a loaded engine
          .map(preProcessingFunction)
          .map(new StdStaticProcessor(engine))
          .name("Execute mapping statements on items")

          // format every list of triples (as strings)
          .flatMap(list => if (list.nonEmpty) Some(list.reduce((a, b) => a + "\n" + b) + "\n\n") else None)
          .name("Convert triples to strings")
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
  private def createTMWithPTMPipeline(triplesMaps: List[JoinedTriplesMap])(implicit env: ExecutionEnvironment, senv: StreamExecutionEnvironment, postProcessor: PostProcessor): DataSet[String] = {
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


      val parentTriplesMap = TriplesMapsCache.get(tm.parentTriplesMap).get;
      val parentDataset =
      // Create a Source from the parents logical source
        Source(parentTriplesMap.logicalSource).asInstanceOf[FileDataSet]
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
          .map(new JoinedStaticProcessor(engine)).name("Execute mapping statements on joined items")

          // format the list of triples as strings
          .flatMap(list => if (list.nonEmpty) Some(list.reduce((a, b) => a + "\n" + b)) else None)
          .name("Convert triples to strings")

      } else { // if there are no join conditions a cross join will be executed

        // create a crossed data set
        val crossed = childDataset.cross(parentDataset)

        crossed.map(items =>
          JoinedItem(items._1, items._2)
        ) // create a JoinedItem from the crossed items
          .map(new JoinedStaticProcessor(engine)).name("Execute mapping statements on joined items") // process the joined items
          .flatMap(list => if (list.nonEmpty) Some(list.reduce((a, b) => a + "\n" + b)) else None) // format the triples
          .name("Convert joined triples to strings")
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


}
