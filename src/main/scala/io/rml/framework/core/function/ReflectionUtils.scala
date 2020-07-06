package io.rml.framework.core.function

import java.lang.reflect.{Method, Parameter, Type, TypeVariable}
import java.net.URL

import scala.reflect.internal.util.ScalaClassLoader

object ReflectionUtils {

  def loadJar(url : URL) = {
    throw new NotImplementedError()
  }
  def loadClass(className : String) = {
    val loadedClassOption = ScalaClassLoader.contextLoader.tryToInitializeClass(className)
    if(loadedClassOption.isDefined)
      loadedClassOption.get
    else
      throw new Exception(s"unable to load ${className}")
  }

  def searchByMethodNameAndParameterTypes(cls : Class[_], methodName : String, parameterTypes : Class[_]*) : Option[Method] = {
    try {
      Some(cls.getDeclaredMethod(methodName, parameterTypes:_*))
    }
    catch {
      case nme: NoSuchMethodException => {
        println(s"Unable to find ${methodName} using cls.getDeclaredMethod; returning the FIRST occurrence by name")
        filterByMethodName(cls, methodName).headOption
      }
    }
  }

  def filterByMethodName(cls : Class[_], methodName : String) : List[Method] = {
    cls.getDeclaredMethods.filter(m=>m.getName.endsWith(methodName)).toList
  }

  def castUsingGenericMethodParameterTypes(method : java.lang.reflect.Method, inputParameterValues : List[AnyRef]) = {
    println(s"${getClass.getCanonicalName}#castUsingGenericMethodParameterTypes")

    val genericParameterTypes = method.getGenericParameterTypes
    genericParameterTypes
      .zip(inputParameterValues)
      .map(
        pair => {
          val gt = pair._1
          val ip = pair._2
          gt.getTypeName match {
            case "java.util.List<java.lang.String>" => {
              val resultArrayList = new java.util.ArrayList[String]()
              ip.asInstanceOf[List[_]].foreach(e=>resultArrayList.add(e.toString))
              resultArrayList
            }
            case _ => ip
          }
        }
    )
  }

  /**
   * Given a method and inputParameterValues, castUsingMethodParameterTypes will use the given method's parameter types
   * to cast the input parameter values.
 *
   * @param method
   * @param inputParameterValues
   * @return
   */
  def castUsingMethodParameterTypes(method : java.lang.reflect.Method, inputParameterValues : List[AnyRef]) = {
    println(s"${getClass.getCanonicalName}#castUsingMethodParameterTypes")



    val parameterTypes = method.getParameterTypes
    parameterTypes
      .zip(inputParameterValues)
      .map(
        pair => {
          val gt = pair._1
          val ip = pair._2
          gt.getTypeName match {
            case "java.util.List" => {
              val resultArrayList = new java.util.ArrayList[Object]()
              ip.asInstanceOf[List[_]].foreach(e=>resultArrayList.add(e.toString))
              resultArrayList
            }
            case "java.lang.Boolean"|"Boolean" => ip.toString.toBoolean
            case _ => ip
          }
        }
      )
      .map(_.asInstanceOf[AnyRef])
  }

}
