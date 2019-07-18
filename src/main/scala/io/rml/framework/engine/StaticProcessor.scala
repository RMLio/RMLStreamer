package io.rml.framework.engine

import io.rml.framework.engine.statement.StatementEngine
import io.rml.framework.flink.item.{Item, JoinedItem}

abstract class StaticProcessor[T<:Item](engine: StatementEngine[T]) (implicit postProcessor: PostProcessor) extends  Processor[T, T](engine) {

  override def map(in: T): List[String] = {
    val triples = engine.process(in)

    postProcessor.process(triples)
  }
}





// Custom processing class with normal items
class StdStaticProcessor(engine: StatementEngine[Item])(implicit postProcessor: PostProcessor) extends StaticProcessor[Item](engine)


// Custom processing class with joined items
class JoinedStaticProcessor(engine: StatementEngine[JoinedItem])(implicit postProcessor: PostProcessor) extends StaticProcessor[JoinedItem](engine)

