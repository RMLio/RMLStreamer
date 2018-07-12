package io.rml.framework.core.model


/**
  * A term node represents blanks and uri's.
  *
  * This trait is used in cases like subject terms where the subject can only be
  * a uri or a blank but never a literal type.
  */
trait TermNode extends Entity {

}
