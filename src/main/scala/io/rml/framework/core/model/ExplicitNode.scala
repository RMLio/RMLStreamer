package io.rml.framework.core.model

/**
  * A explicit node represents uri's and literals
  *
  * This trait is used in cases like data source where the reference identifying the data source can only be
  * uri's or literals (i.e.,  file paths) but never a blank node.
  */
trait ExplicitNode extends Node
