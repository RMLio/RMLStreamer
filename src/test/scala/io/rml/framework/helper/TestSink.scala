package io.rml.framework.helper

import java.util

import org.apache.flink.streaming.api.functions.sink.SinkFunction

/**
  * This object will collect the output of rml generation from a data stream into
  * list of string. The list will be synchronized across the threads.
  */
object TestSink {
  var triples: List[String] = List[String]()

  def apply(): TestSink = new TestSink()
}

class TestSink extends SinkFunction[String] {
  override def invoke(value: String): Unit = {

    synchronized {

      val splitString = value.split('\n')
      TestSink.triples ++= splitString
    }
  }

}
