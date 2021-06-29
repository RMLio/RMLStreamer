package io.rml.framework.flink.sink

import io.rml.framework.core.extractors.NodeCache
import io.rml.framework.core.model.{DataTarget, FileDataTarget, LogicalTarget, Uri}
import io.rml.framework.core.vocabulary.CompressionVoc
import io.rml.framework.flink.bulkwriter.{CompressionBulkWriter, GZIPBulkWriter, ZipBulkWriter}
import io.rml.framework.shared.RMLException
import org.apache.flink.api.common.serialization.{BulkWriter, SimpleStringEncoder}
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.core.fs.{FSDataOutputStream, Path}
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.BasePathBucketAssigner
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.OnCheckpointRollingPolicy
import org.apache.flink.streaming.api.functions.sink.filesystem.{OutputFileConfig, StreamingFileSink}
import org.apache.flink.streaming.api.scala.{DataStream, OutputTag}
import org.apache.flink.util.Collector

import scala.collection.mutable.Map

/**
  * MIT License
  *
  * Copyright (C) 2017 - 2021 RDF Mapping Language (RML)
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
  * */
object TargetSinkFactory {

  /**
    * Creates an output sink for for every logical target fount in the [[NodeCache]].
    * @return A map (logical target ID -> output sink function)
    */
  def createStreamSinksFromLogicalTargetCache(): Map[String, SinkFunction[String]] = {
    val logicalTargetId2Sink: Map[String, SinkFunction[String]] = Map.empty
    NodeCache.logicalTargetIterator.foreach(identifier2target => {
      val identifier = identifier2target._1
      val logicalTarget: LogicalTarget = identifier2target._2
      val dataTarget: DataTarget = logicalTarget.target
      val sink: SinkFunction[String] = dataTarget match {
        case fileDataTarget: FileDataTarget => createFileStreamSink(fileDataTarget, logicalTarget.compression)
        case _ => throw new RMLException(s"${dataTarget.getClass.toString} not supported as data target.")
      }
      logicalTargetId2Sink += identifier -> sink
    })
    logicalTargetId2Sink
  }

  /**
    * Appends all logical target sinks to the given stream by writing records of the stream to side outputs.
    * Earch side output has a tag that corresponds to a logical target ID.
    * @param logicalTargetId2Sinks  Maps a logical target ID to a sink
    * @param dataStream             The given data stream
    * @return                       A stream with records that are *not* written to any logical target,
    *                               i.e., they must be written to the output given as the command line argument.
    */
  def appendSinksToStream(logicalTargetId2Sinks: Map[String, SinkFunction[String]], dataStream: DataStream[Iterable[(String, String)]]): DataStream[String] = {

    // "split" the stream according to logicalTargetId (which becomes the output tag)
    val mainDataStream = dataStream.process(new ProcessFunction[Iterable[(String, String)], String] {
      override def processElement(logicalTargetId2OutputStrings: Iterable[(String, String)], ctx: ProcessFunction[Iterable[(String, String)], String]#Context, out: Collector[String]): Unit = {
        logicalTargetId2OutputStrings.foreach(logicalTargetId2OutputString => {
          val logicalTargetId = logicalTargetId2OutputString._1
          val outputString = logicalTargetId2OutputString._2
          if (logicalTargetId.equals("default")) {
            out.collect(outputString)
          } else {
            ctx.output(OutputTag[String](logicalTargetId), outputString)
          }
        })
      }
    })

    // create side output stream for every logicalTargetId and append the sink.
    logicalTargetId2Sinks.foreach(logicalTargetId2Sink => {
      val logicalTargetId = logicalTargetId2Sink._1
      val sink = logicalTargetId2Sink._2
      val sideOutputStream = mainDataStream.getSideOutput(OutputTag[String](logicalTargetId))
      sideOutputStream.addSink(sink)
    })

    mainDataStream
  }

  private def createFileStreamSink(fileDataTarget: FileDataTarget, compression: Option[Uri]): SinkFunction[String] = {
    createFileStreamSink(fileDataTarget.uri.value, compression)
  }

  private def createFileStreamSink(outputPath: String, compression: Option[Uri]): SinkFunction[String] = {
    val parts = outputPath.split('.')
    val path = parts.slice(0, parts.length - 1).mkString(".")
    var suffix = if (parts.length > 1) {
      "." ++ parts.slice(1, parts.length).mkString(".")
    } else {
      ""
    }
    if (compression.isDefined) {
      suffix += (compression.get.toString match {
        case CompressionVoc.Class.GZIP => ".gz"
        case CompressionVoc.Class.ZIP => ".zip"
        case _ => ""
      })
    }

    if (compression.isEmpty || compression.get.toString == CompressionVoc.Class.TARGZIP
      || compression.get.toString == CompressionVoc.Class.TARXZ)
      StreamingFileSink.forRowFormat(new Path(path), new SimpleStringEncoder[String])
        .withBucketAssigner(new BasePathBucketAssigner[String])
        .withRollingPolicy(OnCheckpointRollingPolicy.build())
        .withOutputFileConfig(OutputFileConfig
          .builder()
          .withPartSuffix(suffix)
          .build())
        .build()
    else
      StreamingFileSink.forBulkFormat(new Path(path), new BulkWriter.Factory[String] {
        override def create(out: FSDataOutputStream): BulkWriter[String] = {
          compression.get.toString match {
            case CompressionVoc.Class.GZIP => new GZIPBulkWriter(out)
            case CompressionVoc.Class.ZIP => new ZipBulkWriter(out)
          }
        }
      }).withBucketAssigner(new BasePathBucketAssigner[String])
        .withRollingPolicy(OnCheckpointRollingPolicy.build())
        .withOutputFileConfig(OutputFileConfig
          .builder()
          .withPartSuffix(suffix)
          .build())
        .build()
  }
}
