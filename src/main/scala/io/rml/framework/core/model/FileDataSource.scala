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

package io.rml.framework.core.model

import java.io.File

import io.rml.framework.core.internal.Logging
import io.rml.framework.core.model.std.StdFileDataSource
import io.rml.framework.shared.RMLException

/**
  *
  */
trait FileDataSource extends DataSource

/**
  * FileDataSource companion object.
  */
object FileDataSource extends Logging {

  /**
    * Factory method for creating a file data source.
    *
    * @param uri Uri that represents the path of the source.
    * @return An instance of DataSource.
    */
  def apply(uri: ExplicitNode): DataSource = {
    val file = new File(uri.toString)
    if (file.isAbsolute) {
      logDebug(Uri(file.getAbsolutePath).uri)
      StdFileDataSource(Uri(file.getAbsolutePath))
    } else {
      val url = ClassLoader.getSystemResource(uri.toString)
      if (url == null) throw new RMLException(uri.toString + " can't be found.")
      val file_2 = new File(url.toURI)
      StdFileDataSource(Uri(file_2.getAbsolutePath))
    }
  }

}
