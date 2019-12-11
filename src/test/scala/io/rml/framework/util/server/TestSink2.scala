package io.rml.framework.util.server

import io.rml.framework.util.logging.Logger
import org.apache.flink.streaming.api.functions.sink.SinkFunction

/**
  * <p>Copyright 2019 IDLab (Ghent University - imec)</p>
  *
  * @author Gerald Haesendonck
  */

object TestSink2 extends SinkFunction[String] {
  private var triples: List[String] = List[String]()

  def apply(): TestSink2 = {
    triples = List[String]()
    new TestSink2()
  }

  def getTriples(): List[String] = {
    triples
  }
}

class TestSink2 extends SinkFunction[String] {
  import TestSink2._

  override def invoke(value: String, context: SinkFunction.Context[_]): Unit = {
    Logger.logInfo(s"TestSink2: got value [${value}]")
    if (!value.trim.isEmpty) {
      triples = value :: triples
    }
  }
}
