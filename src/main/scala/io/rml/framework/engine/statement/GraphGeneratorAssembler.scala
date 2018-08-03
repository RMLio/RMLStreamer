package io.rml.framework.engine.statement

import io.rml.framework.core.model.{TermMap, Uri}
import io.rml.framework.flink.item.Item

class GraphGeneratorAssembler extends TermMapGeneratorAssembler {

  def assemble(graphMapOpt: Option[TermMap]): Item => Option[Iterable[Uri]] = {
    graphMapOpt match {
      case None => Item => None
      case Some(map) => assemble(map)
    }
  }


  override def assemble(termMap: TermMap): Item => Option[Iterable[Uri]] =

    super.assemble(termMap).asInstanceOf[(Item) => Option[Iterable[Uri]]]


}

object GraphGeneratorAssembler {
  def apply(): GraphGeneratorAssembler = new GraphGeneratorAssembler()
}
