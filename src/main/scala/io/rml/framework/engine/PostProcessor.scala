package io.rml.framework.engine

trait PostProcessor extends Serializable{

  def process(quadStrings: List[String]): List[String]
}

class NopPostProcessor extends PostProcessor {
  override def process(quadStrings: List[String]): List[String] = {
    quadStrings
  }
}


class BulkPostProcessor extends PostProcessor {
  override def process(quadStrings: List[String]): List[String] = {
    List(quadStrings.mkString("\n"))

  }
}

class JsonLDProcessor extends PostProcessor {
  override def process(quadStrings: List[String]): List[String] = {
    List()
  }
}