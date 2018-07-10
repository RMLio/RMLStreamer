package io.rml.framework.helper

trait Filter[T] {
  def check(line:T): Boolean
}
