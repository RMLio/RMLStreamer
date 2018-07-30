/*
 * Copyright (c) 2017 Ghent University - imec
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.rml.framework.engine

import io.rml.framework.core.model.Literal
import io.rml.framework.flink.item.Item
import org.scalatest.{FunSuite, Matchers}

class EngineTest extends FunSuite with Matchers {

  private val mockItem : Item =  new Item {
    override def refer(reference: String): Option[List[String]] = {
      if(reference.equals("reference")) Some(List("value"))
      else None
    }
  }

  test("testProcessTemplate") {
    // ============================================================================================
    // Test info
    // ============================================================================================

    /**
      * Tests the normal flow.
      */

    // ============================================================================================
    // Test setup
    // ============================================================================================

    val literal = Literal("https://example.io/{reference}")

    // ============================================================================================
    // Test execution
    // ============================================================================================

    val result: Option[Iterable[String]] = Engine.processTemplate(literal, mockItem)

    // ============================================================================================
    // Test verification
    // ============================================================================================

    result shouldNot be (None)
    result.get should be (List("https://example.io/value"))

  }

  test("testProcessReference") {
    // ============================================================================================
    // Test info
    // ============================================================================================

    /**
      * Tests the normal flow.
      */

    // ============================================================================================
    // Test setup
    // ============================================================================================

    val reference : Literal = Literal("reference")

    // ============================================================================================
    // Test execution
    // ============================================================================================

    val result = Engine.processReference(reference, mockItem)

    // ============================================================================================
    // Test verification
    // ============================================================================================

    result shouldNot be (None)
    result.get should be (List("value"))

  }

}
