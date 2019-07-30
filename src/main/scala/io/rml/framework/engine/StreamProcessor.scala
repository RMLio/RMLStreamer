package io.rml.framework.engine

import io.rml.framework.engine.statement.StatementEngine
import io.rml.framework.flink.item.{Item, JoinedItem}

abstract class StreamProcessor[T<:Item](engine: StatementEngine[T]) (implicit postProcessor: PostProcessor) extends  Processor[T, Iterable[T]](engine) {
  override def map(in: Iterable[T]): List[String] = {
    if (in.isEmpty) return List()

    try {
      val triples = in flatMap engine.process
      postProcessor.process(triples)


    }catch{
      case e => println(e)
                List()
    }
  }
}


// Custom processing class with normal items
class StdStreamProcessor(engine: StatementEngine[Item])(implicit postProcessor: PostProcessor) extends StreamProcessor[Item](engine)


// Custom processing class with joined items
class JoinedStreamProcessor(engine: StatementEngine[JoinedItem])(implicit postProcessor: PostProcessor) extends StreamProcessor[JoinedItem](engine)

