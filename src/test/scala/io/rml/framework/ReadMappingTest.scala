package io.rml.framework

import java.io.File

import io.rml.TestTags.MappingReadSeqTest
import io.rml.framework.helper.Logger
import io.rml.framework.helper.fileprocessing.MappingTestHelper
import io.rml.framework.shared.RMLException
import org.scalatest.{FlatSpec, Matchers}

import scala.util.control.Exception

class ReadMappingTest extends FlatSpec with Matchers{

  var preFailedTestCases: Array[File] =  Array[File]()

  "Reading Mapping File" must "not fail if mapping file is valid" in {

    val mappingFiles = getMappingFilesInFolder("rml-testcases")
    mappingFiles.foreach(file => {
      Logger.logInfo(" Reading mapping file: \n" + file)
      MappingTestHelper.processFile(file)
    })

  }

  it should "throw exceptions if mapping file is not valid (Automated test)" taggedAs MappingReadSeqTest in {
    assertThrows[Exception] {
      val mappingFiles = getMappingFilesInFolder("failing_test_cases")
      val testAmt = mappingFiles.length

      var failedTestCases = Array[File]()
      var passedTestCases = Array[File]()

      for (file <- mappingFiles) {
        Logger.logInfo(" Reading failed mapping file: \n" + file)
        val catcher =  Exception.catching(classOf[RMLException], classOf[Throwable])
        val tryProcessFile = catcher.either(MappingTestHelper.processFile(file))

        
        if(tryProcessFile.isLeft){
          Logger.logSuccess(s"File: $file has passed the test")
          Logger.logError("Exception Log: " +  tryProcessFile.left.get.getMessage)
          passedTestCases :+= file
        }else{
          Logger.logError(s"File: $file has failed the test")
          failedTestCases :+= file
          preFailedTestCases :+= file
        }
        Logger.lineBreak()
      }

      failedTestCases =  failedTestCases.sortWith((a, b) => a.getParentFile.getName  > b.getParentFile.getName)
      passedTestCases =  passedTestCases.sortWith((a, b) => a.getParentFile.getName  > b.getParentFile.getName)
      Logger.logInfo("The following files failed the test: \n "  + failedTestCases.mkString("\n"))
      Logger.logSuccess("The following files have passed the test: \n "  +  passedTestCases.mkString("\n"))

      if (passedTestCases.length > 0) {
        throw new Exception()
      }

    }
  }

  /**
    * The generator will sometime ignore exceptions and just log it when extracting invalid triple map.
    * Supposed the test case is run to check if the generator throws an exception, it will fail since the exception is just logged
    * and not propagated.
    * This method will rerun the test on failed cases, to visually identify in the log, if the test really failed.
    *
    * See @StdTripleMapExtractor for an example of exceptions being just logged
    */

  it should "log exceptions thrown if it doesn't propagate the exceptions" taggedAs MappingReadSeqTest in {

    for (file <- preFailedTestCases ){
      MappingTestHelper.processFile(file)
      Logger.logInfo("File being manually tested: " + file)
      Logger.lineBreak()
    }

    pending
  }




  /**
    * Get mapping ttl files in a directory recursively
    *
    * @param parentFolder the root folder containing subdirectories which in turn contains mapping turtle files
    * @return Array[File] containing mapping turtle files
    */
  def getMappingFilesInFolder(parentFolder: String): Array[File] = {
    var files = Array[File]()
    for (path <- MappingTestHelper.getTestCaseFolders(parentFolder)) {
      val foundFiles = MappingTestHelper.getHelperSpecificFiles(path.toString)

      files = files ++ foundFiles.filter(_.exists())
    }

    files
  }
}
