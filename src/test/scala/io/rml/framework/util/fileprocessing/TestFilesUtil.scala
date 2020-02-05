package io.rml.framework.util.fileprocessing

import java.io.File
import java.nio.file.Path

import io.rml.framework.core.util.Util
import io.rml.framework.util.logging.Logger

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
  def test(rootDir: String, checkFunc: String => Unit): Unit = {
    var checkedTestCases = Array("")
    for (pathString <- getTestCaseFolders(rootDir).map(_.toString).sorted) {

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

  def processFilesInTestFolder(testFolderPath: String): List[R] = {

    val files = getHelperSpecificFiles(testFolderPath)

    files
      .filter(_.exists())
      .map(processFile)
      .toList
  }

}
