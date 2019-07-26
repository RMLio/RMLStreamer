package io.rml.framework

import org.scalatest._

trait TestSpec extends Matchers with OptionValues with Inside with Inspectors


abstract class StaticTestSpec extends FlatSpec with TestSpec


abstract class StreamTestSpec extends AsyncFlatSpec with TestSpec