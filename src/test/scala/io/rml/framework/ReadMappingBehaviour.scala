package io.rml.framework

import java.io.File

import io.rml.framework.shared.RMLException
import io.rml.framework.util.fileprocessing.MappingTestUtil
import io.rml.framework.util.logging.Logger
import org.scalatest.FlatSpecLike

import scala.util.control.Exception

/**
  * A stackable fixture which will be used to check the mapping files in the respective test folders.
  * http://www.scalatest.org/user_guide/sharing_tests
  */
trait ReadMappingBehaviour { this : FlatSpecLike =>

  def validMappingFile(rootDir: String) {
    it must "not fail if mapping file is valid" in {

      val mappingFiles = getMappingFilesInFolder(rootDir)
      mappingFiles.foreach(file => {
        Logger.logInfo(" Reading mapping file: \n" + file)
        MappingTestUtil.processFile(file)
      })

    }
  }
   def invalidMappingFile(rootDir: String) {
    it should "throw exceptions if mapping file is not valid (Automated test)" in {
      assertThrows[Exception] {
        val mappingFiles = getMappingFilesInFolder(rootDir)

        var failedTestCases = Array[File]()
        var passedTestCases = Array[File]()

        for (file <- mappingFiles) {
          Logger.logInfo(" Reading failed mapping file: \n" + file)
          val catcher = Exception.catching(classOf[RMLException], classOf[Throwable])
          val tryProcessFile = catcher.either(MappingTestUtil.processFile(file))


          if (tryProcessFile.isLeft) {
            Logger.logSuccess(s"File: $file has passed the test")
            Logger.logInfo("Exception Log: " + tryProcessFile.left.get.getMessage)
            passedTestCases :+= file
          } else {
            Logger.logInfo(s"File: $file has failed the test")
            failedTestCases :+= file
          }
          Logger.lineBreak()
        }

        failedTestCases = failedTestCases.sortWith((a, b) => a.getParentFile.getName > b.getParentFile.getName)
        passedTestCases = passedTestCases.sortWith((a, b) => a.getParentFile.getName > b.getParentFile.getName)
        Logger.logInfo("The following files failed the test: \n " + failedTestCases.mkString("\n"))
        Logger.logSuccess("The following files have passed the test: \n " + passedTestCases.mkString("\n"))

        if (failedTestCases.length == 0) {
          throw new Exception()
        }

      }
    }
  }




  /**
    * Get mapping ttl files in a directory recursively
    *
    * @param parentFolder the root folder containing subdirectories which in turn contains mapping turtle files
    * @return Array[File] containing mapping turtle files
    */
  def getMappingFilesInFolder(parentFolder: String): Array[File] = {
    var files = Array[File]()
    for (path <- MappingTestUtil.getTestCaseFolders(parentFolder)) {
      val foundFiles = MappingTestUtil.getHelperSpecificFiles(path.toString)

      files = files ++ foundFiles.filter(_.exists())
    }

    files
  }
}
