package io.rml.framework.core.function.model

import io.rml.framework.api.FnOEnvironment
import io.rml.framework.core.function.{FunctionUtils, ReflectionUtils}
import io.rml.framework.core.model.rdf.SerializableRDFQuad
import io.rml.framework.core.model.{Entity, Literal}

import java.io.File
import java.lang.reflect.Method

import java.io.{File, IOException, ObjectInputStream, ObjectOutputStream}
import java.lang.reflect.Method


/**
 * A dynamic function is loaded from an external jar at-runtime.
 *
 * @param identifier [[String]] used to identify this DynamicFunction
 * @param metaData   contains information required for loading, initializing the function
 */
case class DynamicFunction(identifier: String, metaData: FunctionMetaData) extends Function {

  @transient
  private var optMethod: Option[Method] = None
  
  /**
   * There are two ways in which the method can be loaded:
   * 1. From the FnOEnvironment. This means that the class was by Flink's UserCodeClassLoader was able to retrieve
   *  the class from an external jar (located in FLINK_HOME/lib)
   * 2. From a jar that is bundled with the RMLStreamer.
   * @return
   */
  private def loadMethod() : Option[Method] = {
    val optClass = FnOEnvironment.loadedClassesMap.get(metaData.className)
    if (optClass.isDefined) {
      logDebug(s"The class ${metaData.className} was found in the external jars. Will try to load the function ${metaData.methodName}")
      optMethod = ReflectionUtils.searchByMethodNameAndParameterTypes(optClass.get, metaData.methodName, metaData.inputParam.map(_.paramType): _*)
    } else {
      logInfo(s"Could not find ${metaData.className} for this function in the FnOEnvironment" +
        s" (i.e. the class could not be found in an external jar)." +
        s"If the specified source (${metaData.source}) is included in the RML Streamer's resources, " +
        s"and the class (${metaData.className}) can be found,  " +
        s"then ${metaData.methodName} will be loaded from there.")
      val jarFile = getClass.getClassLoader.getResource(metaData.source).getFile
      val classOfMethod = FunctionUtils.loadClassFromJar(new File(jarFile), metaData.className)
      optMethod = ReflectionUtils.searchByMethodNameAndParameterTypes(classOfMethod, metaData.methodName, metaData.inputParam.map(_.paramType): _*)
    }
    optMethod
  }

  override def execute(paramTriples: List[SerializableRDFQuad]): Option[Iterable[Entity]] = {
    // if a group (key: uri) results in a list with 1 element, extract that single element
    // otherwise, when a group has a list with more than 1 element, keep it as a list
    val argResourcesGroupedByUri = paramTriples.groupBy(_.predicate).map {
      pair => {
        pair._2.length match {
          case 0 => pair._1.uri -> None
          case 1 => pair._1.uri -> pair._2.head
          case _ => pair._1.uri -> pair._2.toList
        }
      }
    }

    val argObjectsGroupedByUri = argResourcesGroupedByUri.map {
      pair => {
        if (pair._2.isInstanceOf[Iterable[SerializableRDFQuad]]) {
          pair._1 -> pair._2.asInstanceOf[Iterable[SerializableRDFQuad]].map(x => x.`object`.value.value)
        } else {
          pair._1 -> pair._2.asInstanceOf[SerializableRDFQuad].`object`.value.value
        }

      }
    }

    val orderedArgValues = metaData
      .inputParam
      .sortBy(_.position).flatMap(ip => {

      if (argObjectsGroupedByUri.contains(ip.paramUri))
        argObjectsGroupedByUri.get(ip.paramUri)
      else
        Some(null)

    })

    if (orderedArgValues.size == metaData.inputParam.size) {

      val method = loadMethod.getOrElse(throw new Exception("No method was initialized."))
      val castParameterValues: Array[AnyRef] = ReflectionUtils.castUsingMethodParameterTypes(method, orderedArgValues)

      try {
        val output = method.invoke(null, castParameterValues: _*)
        val result = metaData.outputParam
          .flatMap(elem => elem.getValue(output))
          .map (elem => {
            Literal(elem.toString)
          })
        Some(result)
      } catch {
        case e: Throwable => {
          logError(s"The following exception occurred when invoking the method ${method.getName}: ${e.getMessage}." +
            s"\nThe result will be set to None.", e)
          None
        }
      }
    }
    else None


  }
}
