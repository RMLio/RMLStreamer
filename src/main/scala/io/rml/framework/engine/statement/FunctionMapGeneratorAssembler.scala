package io.rml.framework.engine.statement

import io.rml.framework.api.RMLEnvironment
import io.rml.framework.core.model._
import io.rml.framework.core.vocabulary.RMLVoc
import io.rml.framework.flink.item.Item
import io.rml.framework.flink.sink.FlinkRDFQuad

case class FunctionMapGeneratorAssembler() extends TermMapGeneratorAssembler {

  override def assemble(termMap: TermMap): (Item) => Option[Iterable[Entity]] = {
    require(termMap.isInstanceOf[FunctionMap], "Wrong TermMap instance.")

    val functionMap = termMap.asInstanceOf[FunctionMap]
    val functionEngine = StatementEngine.fromTripleMaps(List(functionMap.functionValue))
    (item: Item) => {
      val triples: List[FlinkRDFQuad] = functionEngine.process(item)
      val parameters: Map[Uri, String] = triples.filter(triple => triple.predicate.uri != Uri(RMLVoc.Property.EXECUTES))
        .map(triple => {
          val parameterName = triple.predicate.uri
          val parameterValue = triple.`object`.value.toString
          parameterName -> parameterValue
        })
        .toMap

      val name: Uri = Uri(triples.filter(triple => triple.predicate.uri == Uri(RMLVoc.Property.EXECUTES))
        .head.`object`.value
        .toString)

      require(RMLEnvironment.hasTransformationRegistered(name), "Transformation " + name + " is not registered.")
      RMLEnvironment.getTransformation(name).get.execute(Parameters(parameters))

      //TODO: PLACEHOLDER REMOVE THIS WHEN FUNCTION MAP IS IMPLEMENTED !!!!!
      None  // <---- REMOVE THIS!!!!!!!!!!!!
    }
  }

}
