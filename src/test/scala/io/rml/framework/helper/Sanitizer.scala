package io.rml.framework.helper

object Sanitizer {
  def sanitize[A <: Iterable[String]](colStrings: A): A = colStrings.map(_.trim
                                                                          .replaceAll(" +", " ")
                                                                        )
                                                                    .filter(_.nonEmpty)
                                                                    .asInstanceOf[A]

}
