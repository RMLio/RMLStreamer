package io.rml.framework.core.function.model

import io.rml.framework.api.FnOEnvironment
import io.rml.framework.core.function.{FunctionUtils, ReflectionUtils}
import io.rml.framework.core.model.{Entity, Literal, Uri}
import io.rml.framework.flink.sink.FlinkRDFQuad

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

  @throws(classOf[IOException])
  private def writeObject(out: ObjectOutputStream): Unit = {
    out.defaultWriteObject()
  }

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

  override def execute(paramTriples: List[FlinkRDFQuad]): Option[Iterable[Entity]] = {
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
        if (pair._2.isInstanceOf[Iterable[FlinkRDFQuad]]) {
          pair._1 -> pair._2.asInstanceOf[Iterable[FlinkRDFQuad]].map(x => x.`object`.value.toString)
        } else {
          pair._1 -> pair._2.asInstanceOf[FlinkRDFQuad].`object`.value.toString
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
        val result = metaData.outputParam.flatMap(elem => elem.getValue(output)) map (elem => Literal(elem.toString))
        Some(result)
      } catch {
        case e: Exception => {
          logError(s"The following exception occurred when invoking the method ${method.getName}: ${e.getMessage}." +
            s"\nThe result will be set to None.")
          None
        }
      }
    }
    else None


  }




  @throws(classOf[IOException])
  @throws(classOf[ClassNotFoundException])
  private def readObject(in: ObjectInputStream): Unit = {
    in.defaultReadObject()
    optMethod = None

  }

  override def execute(arguments: Map[Uri, String]): Option[Iterable[Entity]] = {
    val inputParams = metaData.inputParam
    // casted to List[AnyRef] since method.invoke(...) only accepts reference type but not primitive type of Scala
    val paramsOrdered = arguments.groupBy(_._1.uri).map(_._2.asInstanceOf[AnyRef]).toList

    val outputParams = metaData.outputParam

    if (paramsOrdered.size == inputParams.size) {
      val method = optMethod.get
      val castedParameterValues = ReflectionUtils.castUsingMethodParameterTypes(method, paramsOrdered)
      val output = method.invoke(null, castedParameterValues: _*)

      if (output != null) {
        val result = outputParams.flatMap(elem => elem.getValue(output)) map (elem => Literal(elem.toString))
        Some(result)
      } else
        None
    } else {
      //TODO: complain about inputparams size != params ordered
      logError(s"Not all input parameters for ${metaData.methodName} could be bound...")
      None
    }
  }


  override def getMethod: Option[Method] = {
    optMethod
  }
}
