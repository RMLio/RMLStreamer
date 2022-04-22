package io.rml.framework.api

import be.ugent.idlab.knows.functions.agent.{Agent, AgentFactory}
import io.rml.framework.core.internal.Logging

object FnOEnvironment extends Logging{
  private var functionAgent: Option[Agent] = None

  def apply(functionAgent: Agent = AgentFactory.createFromFnO(
    "functions_grel.ttl",
    "fno/functions_idlab.ttl",
    "grel_java_mapping.ttl",
    "fno/functions_idlab_classes_java_mapping.ttl"
  )): Unit = {
    this.functionAgent = Some(functionAgent)
  }

  def getFunctionAgent: Option[Agent] = {
    this.functionAgent
  }
}
