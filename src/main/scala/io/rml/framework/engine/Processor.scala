package io.rml.framework.engine

import io.rml.framework.engine.statement.StatementEngine
import io.rml.framework.flink.item.{Item, JoinedItem}
import org.apache.flink.api.common.functions.RichMapFunction

/**
  * Abstract class for creating a custom processing mapping step in a pipeline.
  * Extends a RichFunction to have access to the RuntimeContext.
  *
  * Takes [[Item]]
  * @param engine statement engine which will be used to process items
  * @param postProcessor post processor to process the generated triples from the items
  * @tparam T has upper bound of [[Item]]
  * @tparam IN specifies the type of the input for the map(..) function
  */
abstract class Processor[T<:Item, IN](engine: StatementEngine[T])(implicit postProcessor: PostProcessor) extends RichMapFunction[IN, List[String]]

