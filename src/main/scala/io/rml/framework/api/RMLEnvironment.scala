package io.rml.framework.api

import java.io.File

import io.rml.framework.core.extractors.MappingReader
import io.rml.framework.core.model.{FormattedRMLMapping, RMLMapping, Uri}
import io.rml.framework.engine.Transformation
import io.rml.framework.flink.item.Item

import scala.collection.mutable.{Map => MutableMap}

object RMLEnvironment {

  private val transformations: MutableMap[Uri, Transformation] = MutableMap()
  private val sources: MutableMap[Uri, Iterable[Item]] = MutableMap()
  private var generatorBaseIRI: Option[String] = None
  private var mappingFileBaseIRI: Option[String] = None

  def setGeneratorBaseIRI(baseIRI: Option[String]) = {
    generatorBaseIRI = baseIRI
  }

  def getGeneratorBaseIRI(): Option[String] = {
    generatorBaseIRI
  }

  def setMappingFileBaseIRI(baseIRI: Option[String]) = {
    mappingFileBaseIRI = baseIRI
  }

  def getMappingFileBaseIRI(): Option[String] = {
    mappingFileBaseIRI
  }

  def loadMappingFromFile(path: String): RMLMapping = {
    val file = new File(path)
    FormattedRMLMapping.fromRMLMapping(MappingReader().read(file))
  }

  def executeMapping(): Unit = ???

  def executeTriplesMap(): Unit = ???

  def registerTransformation(transformation: Transformation): Unit = {
    transformations.put(transformation.name, transformation)
  }

  def registerSource(uri: Uri, iterable: Iterable[Item]): Unit = {
    require(sources.isEmpty, "Processing of only one source supported in API mode.")
    sources.put(uri, iterable)
  }

  def getSource(uri: Uri): Option[Iterable[Item]] = {
    sources.get(uri)
  }

  def getTransformation(uri: Uri): Option[Transformation] = {
    transformations.get(uri)
  }

  def hasTransformationRegistered(uri: Uri): Boolean = {
    transformations.contains(uri)
  }

  def reset(): Unit = {
    sources.clear()
    transformations.clear()
  }

}
