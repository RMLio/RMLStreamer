package io.rml.framework.util

object Sanitizer {
  def sanitize[A <: Iterable[String]](colStrings: A): A = colStrings.map(_.trim
                                                                          .replaceAll(" +", " ")
                                                                          .replaceAll("> \\.", ">.")
                                                                        )
                                                                    .filter(_.nonEmpty)
                                                                    .asInstanceOf[A]

}
