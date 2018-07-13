package io.rml.framework.helper.fileprocessing

import java.io.File
import java.nio.file.Path

import io.rml.framework.helper.Logger

import scala.util.Sorting

/**
  * Trait for getting turtle files from the specified paths and also
  * for finding leaf directories (used mostly to find test case folders)
  *
  * It provides an interface to process files with the given type parameter R
  *
  *
  *
  * @tparam R type of result from  processing a file
  */

trait TestFilesHelper[R] {

  def getHelperSpecificFiles(path:String): Array[File]
  def processFile(file: File): R
  def getFile(path: String): File = {
    val classLoader = getClass.getClassLoader
    val file_1 = new File(path)
    val result = if (file_1.isAbsolute) {
      new File(path)
    } else {
      new File(classLoader.getResource(path).getFile)
    }
    result
  }

  def getTestCaseFolders(parentTestCaseDir: String): Array[Path] = {


    val parentDir = getFile(parentTestCaseDir)

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
  def test(rootDir: String, checkFunc: String => Unit): Unit = {
    var checkedTestCases = Array("")
    for (pathString <- getTestCaseFolders(rootDir).map(_.toString)) {

      checkFunc(pathString)
      val testCase = new File(pathString).getName
      Logger.logSuccess("Passed processing: " + testCase)
      Logger.lineBreak()
      checkedTestCases :+= testCase
    }

    Sorting.quickSort(checkedTestCases)
    Logger.logInfo("Processed test cases: " + checkedTestCases.mkString("\n"))
    Logger.lineBreak()
  }

  def processFilesInTestFolder (testFolderPath : String): List[R]  = {

    val files = getHelperSpecificFiles(testFolderPath)
    files
      .filter(_.exists())
      .map(processFile)
      .toList
  }

}
