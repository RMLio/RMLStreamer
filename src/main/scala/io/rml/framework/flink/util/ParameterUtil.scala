package io.rml.framework.flink.util

import org.apache.flink.api.java.utils.ParameterTool

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

  // common parameters
  val generalParams = Map(
    "path" -> (true, "The path to an RML mapping file. The path must be accessible on the Flink cluster.", "PATH", None),
    "job-name" -> (false, "The name that the job on the Flink cluster will get.", "JOB NAME", Some("RMLStreamer")),
    "base-IRI" -> (false, "The base IRI as defined in the R2RML spec.", "BASE IRI", Some("")),
    "enable-local-parallel" -> (false, "Distribute incoming data records over local task slots.", "true|false", Some("true"))
  );

  // define mutual exclusive parameter groups
  val fileOutputParams = Map("outputPath"-> (true, "The path to an output file.", "OUTPUT PATH", None))
  val socketOutputParams = Map("socket" -> (true, "The host name (or IP address) and port number of the socket to write to.", "HOST:SOCKET", None))

  val kafkaOutputParams = Map(
    "broker-list" -> (true, "A comma separated list of hosts where Kafka runs on", "HOST:PORT[,HOST:PORT...]", None),
    "topic" -> (true, "The name of the Kafka topic to write output to", "TOPIC NAME", None),
    "partition-id" -> (false, "EXPERIMENTAL. The partition id of kafka topic to which the output will be written to.", "PARTITION NAME", None)
  );

  val mutualExclusiveParameterList = List(
    ("File parameters", fileOutputParams),
    ("TCP socket parameters", socketOutputParams),
    ("Kafka parameters", kafkaOutputParams)
  )

  def processParameters(args: Array[String]): Unit = {
    val parameters = ParameterTool.fromArgs(args)
    val paramMap = parameters.toMap
    // TODO here

    printHelp()

  }

  def printHelp() = {
    mutualExclusiveParameterList.foreach(paramMap => {
      val allParams = generalParams ++ paramMap._2
      println(s"${paramMap._1}: ${formatParameterMap(allParams)}")
    })

    println("\nGeneral parameters:")
    printParameterHelp(generalParams)
    mutualExclusiveParameterList.foreach(paramMap => {
      val allParams = paramMap._2
      println(s"${paramMap._1}:")
      printParameterHelp(paramMap._2)
    })
  }

  /*def printUsageLines(): Unit = {
    mutualExclusiveParameterList.foreach(paramMap => {
      val allParams = generalParams ++ paramMap._2
      println(s"${paramMap._1}: ${formatParameterMap(allParams)}")
    })
  }*/

  def formatParameterMap(parameterMap: Map[String, (Boolean, String, String, Option[String])]): String = {
    parameterMap
      .map(entry => {
        val parameter = entry._1
        val (mandatory, explanation, valueName, defaultValue) = entry._2


        if (mandatory) {
          s"--${parameter} ${valueName}"
        } else {
          s"[--${parameter} ${valueName}]"
        }
      })
      .reduce((p1, p2) => s"${p1} ${p2}")
  }

  def printParameterHelp(parameterMap: Map[String, (Boolean, String, String, Option[String])]): Unit = {
    parameterMap.foreach(entry => {
      val parameter = entry._1
      val (mandatory, explanation, valueName, defaultValue) = entry._2
      val defaultValueString = defaultValue match {
        case Some(value) => s"""Default: "$value"."""
        case None => ""
      }
      val mandatoryValueString = if (mandatory) {
        "Mandatory."
      } else {
        "Optional."
      }
      val padding = " %1$-21s %2$1s %3$1s %4$1s"
      println(padding.format(parameter, explanation, defaultValueString, mandatoryValueString))
    })
  }

}
