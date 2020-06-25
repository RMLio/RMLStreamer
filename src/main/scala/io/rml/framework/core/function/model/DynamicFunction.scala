package io.rml.framework.core.function.model

import java.io.{File, IOException, ObjectInputStream, ObjectOutputStream}
import java.lang.reflect.Method

import io.rml.framework.core.function.{FunctionUtils, ReflectionUtils}
import io.rml.framework.core.model.{Entity, Literal, Uri}
import io.rml.framework.flink.sink.FlinkRDFQuad


/**
 * A dynamic function is loaded from an external jar at-runtime.
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


  override def execute(paramTriples: List[FlinkRDFQuad]): Option[Iterable[Entity]] = {
    // if a group (key: uri) results in a list with 1 element, extract that single element
    // otherwise, when a group has a list with more than 1 element, keep it as a list
    val argResourcesGroupedByUri =  paramTriples.groupBy(_.predicate).map{
      pair => {
        pair._2.length match {
          case 0 => pair._1.uri -> None
          case 1 => pair._1.uri -> pair._2.head
          case _ => pair._1.uri -> pair._2.toList
        }
      }
    }

    val argObjectsGroupedByUri = argResourcesGroupedByUri.map{
      pair => {
        if(pair._2.isInstanceOf[Iterable[FlinkRDFQuad]]){
          pair._1 -> pair._2.asInstanceOf[Iterable[FlinkRDFQuad]].map(x=>x.`object`.value.toString)
        }else{
          pair._1 -> pair._2.asInstanceOf[FlinkRDFQuad].`object`.value.toString
        }

      }
    }

    val orderedArgValues = metaData
      .inputParam
      .sortBy(_.position) // iterate over input parameters by position
      .map(
        ip=>
        {

          argObjectsGroupedByUri.get(ip.paramUri)

        }
      ).flatten

    if(orderedArgValues.size == metaData.inputParam.size){
      val method = optMethod.getOrElse(throw new Exception("No method was initialized."))
      val castParameterValues = ReflectionUtils.castUsingMethodParameterTypes(method, orderedArgValues)
      val output = method.invoke(null, castParameterValues:_*)

      if(output!=null) {
        val result = metaData.outputParam.flatMap(elem => elem.getValue(output)) map (elem => Literal(elem.toString))
        Some(result)
      }else
        None
    }
    else None



  }

  private def findMethod(classObject : Class[_], inputParameters : List[Parameter]) : Option[Method] = {
    try {
      Some(classObject.getDeclaredMethod(metaData.methodName, metaData.inputParam.map(_.paramType): _*))
    }catch{
      case e : NoSuchMethodException => {
        this.logWarning(s"Unable to exactly match the function ${metaData.methodName} in ${metaData.className}.\n" +
          s"Let's try to find the method ourself by filtering on method-name ONLY. [TODO]")
        // try to find the method ourselfs
        val declaredMethods = classObject.getDeclaredMethods
        // first filter out methods with the same name
        val filteredMethods = declaredMethods.filter(_.getName == metaData.methodName)
        // Currently, just return the first available method and assume (hope) it will be correct // TODO: more specific method search
        filteredMethods.headOption
      }
    }



  }

  override def initialize(): Function = {
    if(optMethod.isEmpty) {
      val jarFile = getClass.getClassLoader.getResource(metaData.source).getFile

      val classOfMethod = FunctionUtils.loadClassFromJar(new File(jarFile), metaData.className)
      optMethod = findMethod(classOfMethod, metaData.inputParam)
    }
    this
  }
  @throws(classOf[IOException])
  @throws(classOf[ClassNotFoundException])
  private def readObject(in: ObjectInputStream): Unit = {
    in.defaultReadObject()
    optMethod = None
    initialize()
  }

  override def execute(arguments: Map[Uri, String]): Option[Iterable[Entity]] = {
    val inputParams = metaData.inputParam
    // casted to List[AnyRef] since method.invoke(...) only accepts reference type but not primitive type of Scala
    val paramsOrdered = arguments.groupBy(_._1.uri).map(_._2.asInstanceOf[AnyRef]).toList

    val outputParams = metaData.outputParam

    if (paramsOrdered.size == inputParams.size) {
      val method = optMethod.get
      val castedParameterValues = ReflectionUtils.castUsingMethodParameterTypes(method,paramsOrdered)
      val output = method.invoke(null, castedParameterValues: _*)

      if(output!=null) {
        val result = outputParams.flatMap(elem => elem.getValue(output)) map (elem => Literal(elem.toString))
        Some(result)
      }else
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
