/**
  * MIT License
  *
  * Copyright (C) 2017 - 2020 RDF Mapping Language (RML)
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  *
  **/
package io.rml.framework.core.util

/**
  * @author Gerald Haesendonck
  */
object StreamerConfig {
  /**
    * Enabling this allows data items to be generated from data sources in parallel, per task manager. It adds a keyBy
    * operation to the datastream of Strings to distribute the generated over different task slots. The number of
    * used task slots = min(nr of task slots assigned for the job, nr of task slots on a *single* task manager)
    */
  private var executeLocalParallel: Boolean = false

  /**
    * Enable or disable local parallelism
    * @param parallel true: enable, false: disable
    */
  def setExecuteLocalParallel(parallel: Boolean): Unit = {
    executeLocalParallel = parallel
  }

  /**
    * Check if local parallelism is enabled
    * @return true: enabled, false: disabled
    */
  def isExecuteLocalParallel(): Boolean = {
    executeLocalParallel
  }
}
