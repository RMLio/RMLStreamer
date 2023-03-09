package io.rml.framework.core.function

import io.rml.framework.core.function.model.Parameter
import io.rml.framework.core.internal.Logging
import io.rml.framework.core.model.{Entity, Literal, Uri}
import io.rml.framework.core.vocabulary.{RDFVoc, XsdVoc}
import io.rml.framework.shared.RMLException

import java.time.Instant
import java.util.Date

object FunctionUtils extends Logging {

  def typeCastDataType(output: Entity, dataType: Option[Uri]): Option[Entity] = {
    if(dataType.isDefined){
      val typeClass = getTypeClass(dataType.get)
      val castedValue = Parameter(typeClass, output.value).getValue

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
      case Uri(XsdVoc.Type.XSD_DATE) => classOf[Date]
      case _ => throw new RMLException(s"Type ${uri.value} not supported for parameter")
    }
  }


}
