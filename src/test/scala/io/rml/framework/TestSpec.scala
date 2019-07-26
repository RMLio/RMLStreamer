package io.rml.framework

import org.scalatest.{FlatSpec, Inside, Inspectors, Matchers, OptionValues}

abstract  class TestSpec extends FlatSpec with  Matchers with OptionValues  with Inside with Inspectors{

}
