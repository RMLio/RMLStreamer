package io.rml.framework.flink.source

import java.io.{FileInputStream, InputStream}
import java.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import io.rml.framework.flink.item.Item
import io.rml.framework.flink.item.json.JSONItem
import org.apache.flink.api.common.io.{GenericInputFormat, NonParallelInput}
import org.apache.flink.core.io.GenericInputSplit
import org.jsfr.json.compiler.JsonPathCompiler
import org.jsfr.json.provider.JacksonProvider
import org.jsfr.json.{JacksonParser, JsonSurfer}

class JSONInputFormat(path: String, jsonPath: String) extends GenericInputFormat[Item]{

  private var iterator: util.Iterator[Object] = _
  private var inputStream: InputStream = _
  private var numSplits: Int = _

  override def open(inputSplit: GenericInputSplit): Unit = {
    super.open(inputSplit)
    val surfer = new JsonSurfer(JacksonParser.INSTANCE, JacksonProvider.INSTANCE)
    inputStream = new FileInputStream(path)
    iterator = surfer.iterator(inputStream, JsonPathCompiler.compile(jsonPath))
    this.numSplits = numSplits

    for(i <- 0 until inputSplit.getSplitNumber){
      if(iterator.hasNext) {
        iterator.next()
      }
    }
  }

  override def reachedEnd(): Boolean = !iterator.hasNext

  override def nextRecord(reuse: Item): JSONItem = {
    for(i <- 0 until numSplits-1){
      if(iterator.hasNext) {
        iterator.next()
      }
    }
    val _object = iterator.next()
    val asInstanceOf = _object.asInstanceOf[ObjectNode]
    val mapper = new ObjectMapper()
    val map = mapper.convertValue(asInstanceOf, classOf[java.util.Map[String, Object]])

    new JSONItem(map)
  }

  override def close(): Unit = {
    inputStream.close();
    super.close()
  }
}
