package io.rml.framework.core.function.model

import java.io.{File, IOException, ObjectInputStream, ObjectOutputStream}
import java.lang.reflect.Method

import io.rml.framework.core.function.FunctionUtils
import io.rml.framework.core.model.{Entity, Literal, Uri}


/**
 * A dynamic transformer which will use the functions of a class specified in an external jar
 * The information needed to do reflection is contained inside the variable 'transientTransformation' of type [[FunctionMetaData]]
 *
 * @param identifier [[String]] used to identify this DynamicTransformation
 * @param metaData   contains information required for method reflection
 */
case class DynamicMethodFunction(identifier: String, metaData: FunctionMetaData) extends Function {

  @transient
  private var optMethod: Option[Method] = None

  @throws(classOf[IOException])
  private def writeObject(out: ObjectOutputStream): Unit = {
    out.defaultWriteObject()
  }

  override def initialize(): Function = {
    logDebug("intializing function (identifier: %s)".format(this.identifier))

    if(optMethod.isEmpty) {
      logDebug("optMethod is empty -> loading method from jar %s".format(metaData.source))
      val jarFile = getClass.getClassLoader.getResource(metaData.source.toString).getFile

      val classOfMethod = FunctionUtils.loadClassFromJar(new File(jarFile), metaData.className)
      val method = classOfMethod.getDeclaredMethod(metaData.methodName, metaData.inputParam.map(_.paramType): _*)
      optMethod = Some(method)
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
    logDebug("execute")
    if (optMethod.isEmpty) {
      throw new IllegalStateException(s"DynamicTransformation doesn't have the reflected method yet: ${this.identifier}")
    }

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
      val output = definiteMethod.invoke(null, paramsOrdered: _*)

      val result = outputParams.flatMap(elem => elem.getValue(output))map(elem => Literal(elem.toString))

      Some(result)
    } else {
      None
    }
  }

  override def getMethod: Option[Method] = {
    optMethod
  }
}
