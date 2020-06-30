package io.rml.framework.core.function

import java.io.File
import java.net.MalformedURLException
import java.time.Instant

import io.rml.framework.api.RMLEnvironment
import io.rml.framework.core.function.model.Parameter
import io.rml.framework.core.internal.Logging
import io.rml.framework.core.model.{Entity, Literal, Uri}
import io.rml.framework.core.vocabulary.RMLVoc
import io.rml.framework.shared.RMLException

import scala.reflect.internal.util.ScalaClassLoader.URLClassLoader

object FunctionUtils extends Logging {


  /** *
   * TODO: IMPLEMENT THIS THE SCALA WAY SINCE TYPE ERASURE WILL CAUSE PROBLEM WITH REFLECTIONS IN SCALA
   *
   */
  /**
   * See [[http://stackoverflow.com/questions/3216780/problem-reloading-a-jar-using-urlclassloader?lq=1]]
   * for problems relating to unclosed dirty jar file handle if there ever was a need to reload the jar file
   * at runtime.
   *
   * See [[http://stackoverflow.com/questions/8867766/forName()scala-dynamic-object-class-loading]]
   * for loading in scala object using reflection
   *
   * See [[https://medium.com/@giposse/scala-reflection-d835832ed13a]] for a better scala reflection tutorial?
   *
   * @param jarFile
   * @param className
   * @return
   */
  def loadClassFromJar(jarFile: File, className: String): Class[_] = {
    logDebug(s"Loading $className from jar file $jarFile")
    try {
      val classloader = new URLClassLoader(List(jarFile.toURI.toURL), RMLEnvironment.getClass.getClassLoader)
      logDebug(s"Class loader ${classloader.getParent}")
      Class.forName(className, true, classloader)
    } catch {
      case e@(_: MalformedURLException | _: ClassNotFoundException) =>
        logError(e.getMessage)
        throw e
    }
  }

  def typeCastDataType(output: Entity, dataType: Option[Uri]): Option[Entity] = {
    if(dataType.isDefined){
      val typeClass = getTypeClass(dataType.get)
      val castedValue = Parameter(typeClass, output.toString).getValue

      castedValue.map(e => Literal(e.toString))
    }else{
      Some(output)
    }
  }

  def getTypeClass(uri: Uri): Class[_] = {
    uri match {

      case Uri(RMLVoc.Type.XSD_POSITIVE_INTEGER) => classOf[Int]
      case Uri(RMLVoc.Type.XSD_INTEGER) => classOf[Int]
      case Uri(RMLVoc.Type.XSD_INT) => classOf[Int]
      case Uri(RMLVoc.Type.XSD_STRING) => classOf[String]
      case Uri(RMLVoc.Type.XSD_DOUBLE) => classOf[Double]
      case Uri(RMLVoc.Type.XSD_LONG) => classOf[Long]
      case Uri(RMLVoc.Type.XSD_DATETIME) => classOf[Instant]
      case Uri(RMLVoc.Type.XSD_BOOLEAN) => classOf[Boolean]
      case Uri(RMLVoc.Type.RDF_LIST) => classOf[List[_]]
      case Uri(RMLVoc.Type.XSD_ANY) => classOf[Any]
      case Uri(RMLVoc.Type.RDF_OBJECT) => classOf[Any]
      case _ => throw new RMLException(s"Type $uri not supported for parameter")
    }
  }


}
