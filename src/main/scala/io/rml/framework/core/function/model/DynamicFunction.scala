package io.rml.framework.core.function.model

import java.io.{File, IOException, ObjectInputStream, ObjectOutputStream}
import java.lang.reflect.Method

import io.rml.framework.core.function.FunctionUtils
import io.rml.framework.core.model.{Entity, Literal, Uri}


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
    val paramsOrdered = inputParams
      .flatMap(param => {
        val value = arguments.get(param.paramUri)
        value match {
          case Some(string) => param.getValue(string)
          case _ => None
        }
      })
      .map(_.asInstanceOf[AnyRef])

    val outputParams = metaData.outputParam

    if (paramsOrdered.size == inputParams.size) {
      val definiteMethod = optMethod.get

      // let's cast every input parameter value to the corresponding parameter type of the method parameters
      val castedParameterValues =
        definiteMethod
          .getParameterTypes
          .zip(paramsOrdered)
          .map(
              pair => {
                val t = pair._1.getName
                val v = pair._2
                t match {
                  case "java.lang.Boolean"|"Boolean" => v.toString.toBoolean
                  case _ => v
                }
              }
          )
          .map(_.asInstanceOf[AnyRef])
          .toList

      val output = definiteMethod.invoke(null, castedParameterValues: _*)

      if(output!=null) {
        val result = outputParams.flatMap(elem => elem.getValue(output)) map (elem => Literal(elem.toString))
        Some(result)
      }else
        None
    } else {
      //TODO: complain about inputparams size != params ordered
      None
    }
  }

  override def getMethod: Option[Method] = {
    optMethod
  }
}
