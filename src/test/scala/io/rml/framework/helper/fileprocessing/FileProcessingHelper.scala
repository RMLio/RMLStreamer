package io.rml.framework.helper.fileprocessing

trait FileProcessingHelper[A] extends TestFilesHelper[A]{
      def candidateFiles: List[String]
}
