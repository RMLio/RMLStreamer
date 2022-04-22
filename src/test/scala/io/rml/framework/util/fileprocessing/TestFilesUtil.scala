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
package io.rml.framework.util.fileprocessing

import io.rml.framework.core.util.Util
import io.rml.framework.util.logging.Logger

import java.io.File
import java.nio.file.Path
import scala.util.Sorting

/**
  * Trait for getting turtle files from the specified paths and also
  * for finding leaf directories (used mostly to find test case folders)
  *
  * It provides an interface to process files with the given type parameter R
  *
  * @tparam R type of result from  processing a file
  */

trait TestFilesUtil[R] {

  def getHelperSpecificFiles(testCaseFolder: String): Array[File]

  def processFile(file: File): R


  /**
    * Gets RMLTC* folders located inside the parentTestCaseDir  directory.
    *
    * @param parentTestCaseDir directory where RMLTC* test case folders are located
    * @return Array[Path] an array containing the path of all RMLTC* test case folders.
    */
  def getTestCaseFolders(parentTestCaseDir: String): Array[Path] = {


    val parentDir = Util.getFile(parentTestCaseDir)

    parentDir
      .listFiles
      .filter(_.isDirectory)
      .map(_.toPath)
  }

  /**
    * Helper method for looping through the root folder and iterating over each test case folder.
    * The given checkFunc will be evaluated for each test case folder.
    *
    * @param rootDir
    * @param checkFunc
    */
  def test(rootDir: String, shouldPass: Boolean, checkFunc: (String, Boolean) => Unit): Unit = {
    var checkedTestCases = Array("")
    for (pathString <- getTestCaseFolders(rootDir).map(_.toString).sorted) {
      // clear the loaded classes, this prevents an Exception that would occur when using classes
      // from an unloaded class loader
      checkFunc(pathString, shouldPass)
      val testCase = new File(pathString).getName
      Logger.logSuccess("Passed processing: " + testCase)
      Logger.lineBreak()
      checkedTestCases :+= testCase
    }

    Sorting.quickSort(checkedTestCases)
    Logger.logInfo("Processed test cases: " + checkedTestCases.mkString("\n"))
    Logger.lineBreak()
  }

  def processFilesInTestFolder(testFolderPath: String): List[R] = {

    val files = getHelperSpecificFiles(testFolderPath)

    files
      .filter(_.exists())
      .map(processFile)
      .toList
  }

}
