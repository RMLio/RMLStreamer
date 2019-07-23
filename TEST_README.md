

# Test

The following section goes into detail on what the test case folders are and how you could 
run the test scripts for RMLStreamer. 
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

## Kafka Test

You will need to first have the flink server up and running, just as you would before running the RML streamer from CLI.

## Versioning problem
Flink supports kafka connectors, but only for their respective versions.

Ex. FlinkConsumer010/-Producer010 will use kafka clients module from version 0.10 

This prevents us from implementing a dynamic kafka version support since same modules, used by a library with different versions,
get overwritten by maven depending on it's distance in the dependency tree. 

A posssible [solution](http://jesseyates.com/2015/08/17/using-maven-shade-to-run-multiple-versions-in-a-jvm.html) to the problem, will be making an empty module using the aforementioned connectors libraries. 

We could then change the naming of the conflicting dependencies using maven-shade-plugin, and then compiling this wrapper 
module as a library to be used in the streamer. 

It should work but it's not really a solution but a workaound instead.....


 Executing

The main script will execute an integration test on the streamer for the chosen 
kafka version. 

The test scripts will use the configuration file kafka_test.properties:

```
    kafka08.download.link="https://archive.apache.org/dist/kafka/0.8.2.2/kafka_2.10-0.8.2.2.tgz"
    kafka09.download.link="https://archive.apache.org/dist/kafka/0.9.0.1/kafka_2.10-0.9.0.1.tgz"
    kafka010.download.link="http://apache.cu.be/kafka/0.10.2.2/kafka_2.10-0.10.2.2.tgz"
    
    data-input=src/test/resources/stream/datasource.json
    mapping=src/test/resources/stream/mapping.ttl
    flinkBin=/opt/flink-1.8.0/bin/flink
    rdf-test-topic=connect-test  #topic used by flink kafka producer to write output
    
    #the following 3 configs should be the same as the one written in the mapping file. 
    rdf-source-topic=demo   #topic used by kafka cluster for streamer input data
    zookeeper.connection=127.0.0.1:2181
    broker-list=127.0.0.1:9092
```


At this moment you will need to create a test.txt file containing the input data in the 
working directory, for instance `src/test/resources/stream/datasource.json`.

This file will be used by the kafka producer for stream emission. 
 
Executing the main script: 
```
    ./kafka-test-run.sh  [option]
    
    Options:
        -c|--clean: recompiles all the classes using "mvn clean install -DskipTests".
        -v|--verbose: logs all the output of subscripts executed by this main script.
```

The main script is composed of the following child scripts:
1. kafka-test-source-checker.sh 
2. kafka-test-server-setup.sh
3. kafka-test-stop-all.sh

The main script will wait for user input for collecting generated triples and running kafka producer to 
write data from test.txt to the topic specified in the kafka_test.properties 

 Main Script.

While executing the test script, you will have to keep in mind the connector being used in the implementation.

For more info, check out the official [documentation](https://ci.apache.org/projects/flink/flink-docs-release-1.8/dev/connectors/kafka.html)

Here are steps the script will take:

1. The script will first execute kafka-test-source-checker.sh.


2. Choose the desired version then press enter:
```
    1) Kafka08
    2) Kafka09
    3) Kafka010
    Choose the kafka version server for testing...

```

2. kafka-test-server-setup.sh will be executed with default config files provided in the respective kafka bin directory.


3. 2 topics will be created "demo" and "connect-test"


4. RML streamer will be started using the compiled jar in target/ folder just like in the run.sh script. 


5. You will have to wait for the rml streamer to be setup properly and finish setting up connection with the kafka cluster. Wait until rml streamer stops logging to the terminal. 

    In verbose mode you will have to pay attention to the following log in the terminal, to know that the flink job is up and running.

```
    09/14/2018 13:30:34	Source: Custom Source -> Flat Map -> Flat Map -> Map -> Execute statements on items. -> Reduce to strings. -> Sink: Unnamed(1/1) switched to RUNNING 
```

6. You can now press enter to make kafka producer write the data from test.txt to the specified topic in the kafka cluster.

7. From here onwards you will see prompts for user input, just follow them.

The script will at the end executes kafka-test-stop-all.sh to terminate any running kafka server. 

***NOTE***
If you have terminated the script while it is still running, please be sure to kill the server and free up the ports used by them. You can try 
using the scripts provided in the bin/ directory of the specified kafka version. 

If it still doesn't work find the pid of the server processes and terminate it.


command to find pids of zookeeper and broker:
```
    ps -ax | grep -i zookeeper 
    ps -ax | grep -i kafka
```

 What does the scripts do?


```
    ./kafka-test-source-checker.sh
    
    Checks in the current directory for folder containing bins for setting up kafka server. 
    The script will automatically download if it doesn't detect any separate kafka folder containing version 0.8, 0.9 or 0.10. 
    
    If you already have the kafka folders, move them to working directory and name them accordingly. 
    The matches for folder are done using following regexes with grep: 

    kafka*0.8*
    kafka*0.9*
    kafka*0.10*

```

```
    ./kafka-test-server-setup.sh [-d|-zp|-bp] [ARGS]

    It will setup a kafka cluster using the configurations from the property files given.

    Options:
        -d|--kafka-dir: root directory of kafka bin for starting up servers
        -zp|--zookeeper-property: property file containing configurations for the zookeeper 
                                  for reference, see config/zookeeper.properties in kafka dir 
        -bp|--broker-property: property file containing configurations fro the broker 
                               for reference, see config/server.properties in kafka dir 

```

```
    ./kafka-test-stop-all.sh

    Finds the kafka zookeeper/broker process ids written out in /tmp/kafka-test-pids 
    and kill them.

```


## Streaming Tests

The streaming test cases will have to be checked using the script stream-test.sh 

```
    bash stream-test.sh [option]
    
    Options:  
        -c|--clean: recompiles all test classes using mvn test -DskipTests. 
        -t|--type: specifies the type of test [kafka | tcp]
```

The script will run all the test cases of the specified stream type under the "stream" folder of test resources.

Any failed test cases will be identified, in the console, at the end of the script with their 
output. If any test case doesn't get evaluated by the script, running with --clean might solve it. 

## Report Generation 

Once you have implemented new test cases and features, it is advisable to run the report generation `updateResults.sh` script under the {root}/scripts/aux

Execute the script by changing the working directory to the 
{root}/scripts/aux first. Will fix later on to work properly with properties file.

The script takes can be used as follows: 

``` 
Usage: 
updateResults.sh [-t [static|stream]]

Options:
    
    -t|--type: specifies the type of streamer 
               mode for which the test cases will be updated 



``` 







