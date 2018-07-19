package io.rml.framework.util

class CommentFilter extends Filter[String] {
  val filterChars = Set('#')

  override def check(line: String): Boolean = {
      line.trim.length > 0 &&  !filterChars.contains(line.trim.charAt(0))
  }
}
