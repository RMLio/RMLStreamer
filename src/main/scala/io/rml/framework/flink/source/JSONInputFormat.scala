package io.rml.framework.flink.source

import java.io.{BufferedReader, FileReader}
import java.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.jayway.jsonpath.JsonPath
import io.rml.framework.flink.item.Item
import io.rml.framework.flink.item.json.JSONItem
import org.apache.flink.api.common.io.{GenericInputFormat, NonParallelInput}
import org.apache.flink.core.io.GenericInputSplit
import org.jsfr.json.compiler.JsonPathCompiler
import org.jsfr.json.provider.JacksonProvider
import org.jsfr.json.{JacksonParser, JsonSurfer}

import scala.collection.{immutable, mutable}

class JSONInputFormat(path: String, jsonPath: String) extends GenericInputFormat[Item] with NonParallelInput  {

  private var iterator : util.Iterator[Object] = _

  override def open(inputSplit: GenericInputSplit): Unit = {
    super.open(inputSplit)
    val surfer = new JsonSurfer(JacksonParser.INSTANCE, JacksonProvider.INSTANCE)
    iterator = surfer.iterator(new BufferedReader(new FileReader(path)),
                                          JsonPathCompiler.compile(jsonPath))

  }

  override def reachedEnd(): Boolean = !iterator.hasNext

  override def nextRecord(reuse: Item): JSONItem = {
    val _object = iterator.next()
    val asInstanceOf = _object.asInstanceOf[ObjectNode]
    val mapper = new ObjectMapper()
    val map = mapper.convertValue(asInstanceOf, classOf[java.util.Map[String,Object]])
    new JSONItem(map)
  }
}
