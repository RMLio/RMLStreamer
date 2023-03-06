package io.rml.framework.core.function.model

import io.rml.framework.core.model.{Node, Uri}

import java.text.{DecimalFormat, NumberFormat}
import java.time.Instant
import java.util.Locale
import scala.util.parsing.json.JSON

/**
 * Case classes which could be useful in pattern matching empty parameter values
 */

case class EmptyParameter(paramType: Class[_], paramUri: Uri, position: Int) extends Parameter {
  override val paraValue: Option[String] = None

}

case class DefinedParameter(paramType: Class[_] = classOf[String], paramUri: Uri, paraValue: Option[String], position: Int) extends Parameter


abstract class Parameter extends Node {
  /**
   * Models the parameters used by the functions.
   *
   * `paramType` type of the parameter
   * `paramUri`  Uri representation of the parameter
   * `paraValue` [[String]] representation of the parameter
   * `position`  [[Int]] position of the parameter in the argument of the function needed for reflection
   *
   */
  val paramType: Class[_]
  val paramUri: Uri
  val paraValue: Option[String]
  val position: Int

  override def identifier: String = paramUri.value + " " + paraValue.getOrElse("None")

  def getValue: Option[Any] = {
    getValue(paraValue.getOrElse(throw new IllegalStateException(s"${this}'s value option is empty.")))
  }

  /**
   * Get the parameter value and cast it according to the given
   * [[paramType]].
   *
   * @return value of the parameter of type specified by [[paramType]]
   */


  def getValue(paraValue: Any): Option[Any] = {
    val ScalaString = classOf[String].getName
    val IntegerString = classOf[Int].getName
    val DoubleString = classOf[Double].getName
    val ListString = classOf[List[_]].getName
    val ArrayString = classOf[Array[_]].getName
    val ObjectString = classOf[Object].getName
    val BooleanString = classOf[Boolean].getName
    val LongString = classOf[Long].getName
    val InstantString = classOf[Instant].getName

    if(paramType== null)
      throw new NullPointerException("parameter type is null..")
    else{
      paramType.getName match {
        case BooleanString |"boolean" => Some(paraValue)
        case ScalaString | "java.lang.String" => Some(paraValue.toString)
        case IntegerString | "int" => Some(paraValue.toString.toInt)
        case DoubleString | "double" => Some(formatToScientific(paraValue.toString.toDouble))
        case LongString | "long" => Some(paraValue.toString.toLong)
        case InstantString => Some(Instant.parse(paraValue.toString))
        case ObjectString|"java.lang.Object" => Some(paraValue)

        case ListString | ArrayString | "java.util.List" =>

          val parsedListEither = JSON.parseFull(paraValue.toString).toRight("Value can't be parsed as List")

          parsedListEither match {
            case Right(parsed) =>
              parsed match {
                case value: List[_] =>
                  Some(value)
                case _ =>
                  None
              }


        case Left(exMessage) => throw new IllegalArgumentException(exMessage)
          }
        case _ => throw new Error(s"Couldn't derive type: ${paramType.getName}")

      }
    }
  }

  /**
   * Formats the decimal to scientific notation
   * Taken from RMLMapper: be.ugent.rml.Utils
   * @param double double to be cast
   * @return scientific notation of the double
   */
  private def formatToScientific(double: Double): String = {
    val input = BigDecimal.valueOf(double).bigDecimal.stripTrailingZeros()
    val precision =
      if (input.scale() < 0)
        input.precision() - input.scale()
      else
        input.precision()

    val sb = new StringBuilder("0.0")
    for (_ <- 2 to precision) {
      sb.append("#")
    }
    sb.append("E0")
    val decimalFormat = NumberFormat.getNumberInstance(Locale.US).asInstanceOf[DecimalFormat]
    decimalFormat.applyPattern(sb.toString())

    decimalFormat.format(double)
  }





}

/**
 * OBJECT IMPLEMENTATION OF PARAMETER HERE
 */

object Parameter {
  /**
   * Implicit default ordering which will be used to order lists/sequences of [[Parameter]]
   *
   * @tparam A subclasses of [[Parameter]]
   */
  implicit def orderingByPosition[A <: Parameter]: Ordering[A] = {
    Ordering.by(elem => elem.position)
  }

  def apply(paramType: Class[_], paramUri: Uri, paraValue: String, position: Int): Parameter = {
    DefinedParameter(paramType, paramUri, Some(paraValue), position)
  }

  def apply(paramType:Class[_], paraValue:String): Parameter ={
    DefinedParameter(paramType, Uri(""),Some(paraValue), 0)
  }


  def apply(paramType: Class[_], paramUri: Uri, position: Int): Parameter = {
    EmptyParameter(paramType, paramUri, position)
  }

  def apply(paramUri: Uri, paraValue: String, position: Int): Parameter = {
    DefinedParameter(paramUri = paramUri, paraValue = Some(paraValue), position = position)
  }

}

