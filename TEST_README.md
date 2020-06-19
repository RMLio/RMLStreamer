# Test

The following section goes into detail on what the test case folders are.
Running these tests using maven with `mvn test` don't require Flink or Kafka to be installed or started.
The test framework starts a local Flink minicluster and embedded Kafka when needed. 

## Folders

There are 4 types of test case folders:
* rml-original-testcases
* rml-testcases
    * json-ld
* stream
    * kafka
    * tcp
* json-ld 
    * kafka
    * tcp
* negative_test_cases
* temp_ignored_testcases 


1. rml-original-testcases contains all the original test cases without edits in the sub files/folders.  

2. rml-testcases contains all the test cases for which, the current implementation should pass. 
    * json-ld folder contains test cases where the expected output is in json-ld format.

3. stream contains all streaming test cases which should be checked using the stream-test.sh in the root folder.
    * kafka contains all the test cases with kafka cluster as logical source.
    * tcp contains all the test cases with tcp socket as a logical source. 
    
4. json-ld contains the streaming test cases which are checked just like test cases in number 3).
   The expected output is in json-ld format.  
   
5. negative_test_cases contains cases for which, the current implementation should throw exceptions/fail.  

6. temp_ignored_testcases contains cases for which, the current implementation cannot pass due to missing features. 