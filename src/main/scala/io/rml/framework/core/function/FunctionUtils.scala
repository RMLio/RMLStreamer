package io.rml.framework.core.function

import io.rml.framework.api.RMLEnvironment
import io.rml.framework.core.function.model.Parameter
import io.rml.framework.core.internal.Logging
import io.rml.framework.core.model.{Entity, Literal, Uri}
import io.rml.framework.core.vocabulary.{RDFVoc, XsdVoc}
import io.rml.framework.shared.RMLException

import java.io.File
import java.net.MalformedURLException
import java.time.Instant
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

      case Uri(XsdVoc.Type.XSD_POSITIVE_INTEGER) => classOf[Int]
      case Uri(XsdVoc.Type.XSD_INTEGER) => classOf[Int]
      case Uri(XsdVoc.Type.XSD_INT) => classOf[Int]
      case Uri(XsdVoc.Type.XSD_STRING) => classOf[String]
      case Uri(XsdVoc.Type.XSD_DOUBLE) => classOf[Double]
      case Uri(XsdVoc.Type.XSD_LONG) => classOf[Long]
      case Uri(XsdVoc.Type.XSD_DATETIME) => classOf[Instant]
      case Uri(XsdVoc.Type.XSD_BOOLEAN) => classOf[Boolean]
      case Uri(RDFVoc.Type.RDF_LIST) => classOf[List[_]]
      case Uri(XsdVoc.Type.XSD_ANY) => classOf[Any]
      case Uri(RDFVoc.Type.RDF_OBJECT) => classOf[Any]
      case _ => throw new RMLException(s"Type ${uri.value} not supported for parameter")
    }
  }


}
