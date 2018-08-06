package io.rml.framework.engine.statement

object StatementType extends Enumeration{
  type StatementType = Value

  val STANDARD = Value("Standard statement")
  val CHILD = Value("Child statement")
  val PARENT = Value("Parent statement ")
}