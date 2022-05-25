package io.rml.framework.core.util

import io.rml.framework.core.util.ParameterUtil.OutputSinkOption.OutputSinkOption
import io.rml.framework.core.util.ParameterUtil.PostProcessorOption.PostProcessorOption

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
object ParameterUtil {

  case class ParameterConfig(
                              mappingFilePath: String = ".",
                              jobName: String = "RMLStreamer",
                              baseIRI: Option[String] = None,
                              localParallel: Boolean = true,
                              postProcessor: PostProcessorOption = PostProcessorOption.None,
                              checkpointInterval: Option[Long] = None,
                              outputPath: Option[String] = None,
                              brokerList: Option[String] = None,
                              broker: Option[String] = None,
                              topic: Option[String] = None,
                              partitionId: Option[Int] = None,
                              socket: Option[String] = None,
                              outputSink: OutputSinkOption = OutputSinkOption.File,
                              autoWatermarkInterval: Long =  50L,
                              functionDescriptionLocations: Option[Seq[String]] = None
                            ) {
    override def toString: String = {
      val resultStr: String =
        s"Mapping file: ${mappingFilePath}\n" +
        s"Job name: ${jobName}\n" +
        s"Base IRI: ${baseIRI.getOrElse("/")}\n" +
        s"Parallelise over local task slots: ${localParallel}\n" +
        s"Post processor: ${postProcessor}\n" +
        s"Checkpoint interval: ${checkpointInterval.getOrElse("/")}\n" +
          s"Auto Watermark interval: ${autoWatermarkInterval}\n" +
        s"Output method: ${outputSink}\n" +
        s"Output file: ${outputPath.getOrElse("/")}\n" +
        s"Kafka broker list: ${brokerList.getOrElse("/")}\n" +
        s"Kafka topic: ${topic.getOrElse("/")}\n" +
        s"Kafka topic partition id: ${partitionId.getOrElse("/")}\n" +
        s"Output TCP socket: ${socket.getOrElse("/")}\n" +
        s"Function description locations: ${functionDescriptionLocations}\n" +
        s"Discard output: ${outputSink.equals(OutputSinkOption.None)}"
      resultStr
    }
  }

  // possible output sink options
  object OutputSinkOption extends Enumeration {
    type OutputSinkOption = Value
    val File, Socket, Kafka, MQTT, None = Value
  }

  // possible post processor options
  object PostProcessorOption extends Enumeration {
    type PostProcessorOption = Value
    val None, Bulk, JsonLD= Value
  }


  val parser = new scopt.OptionParser[ParameterConfig]("RMLStreamer") {
    override def showUsageOnError = Some(true)

    head("RMLStreamer", "2.1.0-SNAPSHOT")

    opt[String]('j', "job-name").valueName("<job name>")
      .optional()
      .action((value, config) => config.copy(jobName = value))
      .text("The name to assign to the job on the Flink cluster. Put some semantics in here ;)")

    opt[String]('i', "base-iri").valueName("<base IRI>")
      .optional()
      .action((value, config) => config.copy(baseIRI = Some(value)))
      .text("The base IRI as defined in the R2RML spec.")

    opt[Unit]("disable-local-parallel")
      .optional()
      .action((_, config) => config.copy(localParallel = false))
      .text("By default input records are spread over the available task slots within a task manager to optimise parallel processing," +
        "at the cost of losing the order of the records throughout the process." +
        " This option disables this behaviour to guarantee that the output order is the same as the input order.")

    opt[String]('m', "mapping-file").valueName("<RML mapping file>").required()
      .action((value, config) => config.copy(mappingFilePath = value))
      .text("REQUIRED. The path to an RML mapping file. The path must be accessible on the Flink cluster.")


    opt[Unit]("json-ld")
        .optional()
        .action((_, config) => config.copy(postProcessor = PostProcessorOption.JsonLD))
        .text("Write the output as JSON-LD instead of N-Quads. An object contains all RDF generated from one input record. " +
          "Note: this is slower than using the default N-Quads format.")

    opt[Unit]("bulk")
      .optional()
      .action((_, config) => config.copy(postProcessor = PostProcessorOption.Bulk))
      .text("Write all triples generated from one input record at once, instead of writing triples the moment they are generated.")

    opt[Long]("checkpoint-interval").valueName("<time (ms)>")
        .optional()
        .action((value, config) => config.copy(checkpointInterval = Some(value)))
        .text("If given, Flink's checkpointing is enabled with the given interval. " +
          "If not given, checkpointing is enabled when writing to a file (this is required to use the flink StreamingFileSink). " +
          "Otherwise, checkpointing is disabled.")

    opt[Long]("auto-watermark-interval").valueName("<time (ms)>")
      .optional()
      .action((value, config) => config.copy(autoWatermarkInterval = value))
      .text("If given, Flink's watermarking will be generated periodically with the given interval. " +
        "If not given, a default value of 50ms will be used." +
      "This option is only valid for DataStreams.")

    opt[Seq[String]]('f', "function-descriptions").valueName("<function description location 1>,<function description location 2>...")
      .optional()
      .action((values, config) => config.copy(functionDescriptionLocations = Some(values)))
      .text("An optional list of paths to function description files (in RDF using FnO). A path can be a file location or a URL.")

    // options specifically for writing output to file
    cmd("toFile")
      .text("Write output to file \n" +
        "Note: when the mapping consists only of stream triple maps, a StreamingFileSink is used. " +
        "This sink will write the output to a part file at every checkpoint.")
      .action((_, config) => config.copy(outputSink = OutputSinkOption.File))
      .children(
        opt[String]('o', "output-path").valueName("<output file>").required()
          .action((value, config) => config.copy(outputPath = Some(value)))
          .text("The path to an output file. " +
            "Note: when a StreamingFileSink is used (the mapping consists only of stream triple maps), this path specifies a directory and optionally an extension. " +
            "Part files will be written to the given directory and the given extension will be used for each part file.")
      )

    // options specifically for writing output to a Kafka topic
    cmd("toKafka")
      .text("Write output to a Kafka topic")
      .action((_, config) => config.copy(outputSink = OutputSinkOption.Kafka))
      .children(
        opt[String]('b', "broker-list").valueName("<host:port>[,<host:port>]...").required()
          .action((value, config) => config.copy(brokerList = Some(value)))
          .text("A comma separated list of Kafka brokers."),
        opt[String]('t', "topic").valueName("<topic name>").required()
          .action((value, config) => config.copy(topic = Some(value)))
          .text("The name of the Kafka topic to write output to."),
        opt[Int]("partition-id").valueName("<id>").optional()
          .text("EXPERIMENTAL. The partition id of kafka topic to which the output will be written to.")
          .action((value, config) => config.copy(partitionId = Some(value)))
      )

    // options specifically for writing to TCP socket
    cmd("toTCPSocket")
      .text("Write output to a TCP socket")
      .action((_, config) => config.copy(outputSink = OutputSinkOption.Socket))
      .children(
        opt[String]('s', "output-socket").valueName("<host:port>").required()
          .action((value, config) => config.copy(socket = Some(value)))
          .text("The TCP socket to write to.")
      )

    // options specifically for writing to MQTT topic
    cmd("toMQTT")
      .text("Write output to an MQTT topic")
      .action((_, config) => config.copy(outputSink = OutputSinkOption.MQTT))
      .children(
        opt[String]('b', "broker").valueName("<host:port>").required()
          .action((value, config) => config.copy(broker = Some(value)))
          .text("The MQTT broker."),
        opt[String]('t', "topic").valueName("<topic name>").required()
          .action((value, config) => config.copy(topic = Some(value)))
          .text("The name of the MQTT topic to write output to.")
      )

    cmd("noOutput")
      .text("Do everything, but discard output")
      .action((_, config) => config.copy(outputSink = OutputSinkOption.None))
  }

  def processParameters(args: Array[String]): Option[ParameterConfig] = {
    parser.parse(args, ParameterConfig())
  }

}
