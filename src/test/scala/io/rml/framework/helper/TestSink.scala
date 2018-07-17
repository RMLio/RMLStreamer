package io.rml.framework.helper

import java.util

import org.apache.flink.streaming.api.functions.sink.{RichSinkFunction, SinkFunction}

/**
  * This object will collect the output of rml generation from a data stream into
  * list of string. The list will be synchronized across the threads.
  */
object TestSink {
  val triples: util.List[String] = new util.ArrayList[String]()

  def apply(): TestSink = new TestSink()
}

class TestSink extends RichSinkFunction[String] {
   override  def invoke(value: String): Unit = {

    synchronized {
      for (el <- value.split('\n'))
        TestSink.triples.add(el)
    }
  }

}
