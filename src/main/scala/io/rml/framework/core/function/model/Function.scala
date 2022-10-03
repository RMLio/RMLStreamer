package io.rml.framework.core.function.model

import be.ugent.idlab.knows.functions.agent.{Agent, Arguments}
import io.rml.framework.core.internal.Logging
import io.rml.framework.core.model.rdf.SerializableRDFQuad
import io.rml.framework.core.model.{Entity, Literal, Node}

abstract class Function extends Node with Logging{
  def execute(identifier: String, paramTriples: List[SerializableRDFQuad]): Option[Iterable[Entity]]
}

object Function extends Logging{

  def execute(identifier: String, paramTriples: List[SerializableRDFQuad], agent: Agent): Option[Iterable[Entity]] = {
    // if a group (key: uri) results in a list with 1 element, extract that single element
    // otherwise, when a group has a list with more than 1 element, keep it as a list
    val argResourcesGroupedByUri = paramTriples.groupBy(_.predicate).map {
      pair => {
        pair._2.length match {
          case 0 => pair._1.uri -> None
          case _ => pair._1.uri -> pair._2
        }
      }
    }

    try {
      // create Arguments
      val arguments: Arguments = new Arguments();
      argResourcesGroupedByUri.foreach(argPair => {
        val parameterName: String = argPair._1.toString;
        argPair._2.asInstanceOf[List[SerializableRDFQuad]].foreach(quad => {
          val parameterValue: String = quad.`object`.value.value
          arguments.add(parameterName, parameterValue)
        })
      })
      // execute the funtion using the function agent
      val result = agent.execute(identifier, arguments)
      result match {
        case null => None
        case _ => Some(List(Literal(result.toString)))
      }
    } catch {
      case e: Throwable => {
        e.printStackTrace()
        logError(s"The following exception occurred when invoking function ${identifier}: ${e.getMessage}." +
          s"\nThe result will be set to None.", e)
        None
      }
    }
  }
}
