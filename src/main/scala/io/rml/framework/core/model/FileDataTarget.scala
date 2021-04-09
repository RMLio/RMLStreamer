package io.rml.framework.core.model

import io.rml.framework.api.RMLEnvironment
import io.rml.framework.core.internal.Logging
import io.rml.framework.core.model.std.StdFileDataStore

import java.io.File
import java.net.URI
import java.nio.file.Paths

/**
  * MIT License
  *
  * Copyright (C) 2017 - 2021 RDF Mapping Language (RML)
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
  * */
trait FileDataTarget extends DataTarget

object FileDataTarget extends Logging {
  def apply(uri: ExplicitNode): DataTarget = {
    val fileUri = URI.create(uri.value);
    if (fileUri.getScheme.equals("file")) {
      val file = new File(fileUri.getPath)
      if (file.isAbsolute) {
        StdFileDataStore(Uri(file.getCanonicalPath))
      } else {
        // create path relative to mapping file
        val mappingFileParentPath = Paths.get(RMLEnvironment.getMappingFileBaseIRI().get).getParent
        val filePath = mappingFileParentPath.resolve(file.toPath)
        StdFileDataStore(Uri(filePath.toFile.getCanonicalPath))
      }
    } else {
      StdFileDataStore(Uri(uri.value))
    }
  }
}