package io.rml.framework.util

trait Filter[T] {
  def check(line:T): Boolean
}
