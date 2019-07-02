package io.rml.framework.engine

/**
  * Processes the generated triples from one record.
  *
  */
trait PostProcessor extends Serializable{

  def process(quadStrings: List[String]): List[String]
}

/**
  * Does nothing, returns the input list of strings
  */
class NopPostProcessor extends PostProcessor {
  override def process(quadStrings: List[String]): List[String] = {
    quadStrings
  }
}

/**
  *
  * Groups the list of generated triples from one record into one big
  * string.
  */
class BulkPostProcessor extends PostProcessor {
  override def process(quadStrings: List[String]): List[String] = {
    List(quadStrings.mkString("\n"))
  }
}

/**
  *
  * Format the generated triples into json-ld format
  */
class JsonLDProcessor(prefix:String) extends PostProcessor {
  override def process(quadStrings: List[String]): List[String] = {
    List()
  }
}