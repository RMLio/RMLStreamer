## RML Framework

### Installing Flink
The RML Streamer runs its jobs on Flink clusters.
More information on how to install Flink and getting started can be found [here](https://ci.apache.org/projects/flink/flink-docs-release-1.7/tutorials/local_setup.html).
At least a local cluster must be running in order to start executing RML Mappings with the RML Streamer.
It is not necessary to install Hadoop. Please note that the current repository works with Flink 1.8.0 with Scala 2.11 support, which can be downloaded [here](https://www.apache.org/dyn/closer.lua/flink/flink-1.8.0/flink-1.8.0-bin-scala_2.11.tgz).

## Installing RML Framework

Clone or download and then build the code in this repository:

```
git clone https://github.com/RMLio/RMLStreamer.git 
cd RMLStreamer
mvn clean package
```

The resulting `RMLStreamer-<version>.jar` can be deployed on a Flink cluster.

### Executing RML Mappings

The script `run.sh` helps running the RML Streamer on a given Flink cluster.

```
Usage:
run.sh -p RML MAPPING PATH -f FLINK PATH -o FILE OUTPUT PATH [-a PARALLELISM]
run.sh -p RML MAPPING PATH -f FLINK PATH -s SOCKET [-a PARALLELISM] 
run.sh -p RML MAPPING PATH -f FLINK PATH -b KAFKA BROKERS -t KAFKA TOPIC
run.sh -c CONFIG FILE

Every option can be defined in its long form in the CONFIG FILE.
E.g. flinkBin=/opt/flink-1.8.0/flink

Options:
-p --path RML MAPPING PATH         The path to an RML mapping file.
-o --outputPath FILE OUTPUT PATH   The path to an output file.
-f --flinkBin FLINK PATH           The path to the Flink binary.
-s --socket                        The port number of the socket.
-b --kafkaBrokerList KAFKA BROKERS The (list of) hosts where Kafka runs on
-a --parallelism                   The parallelism to assign to the job. The default is 1.
-pp --post-process                 The name of the post processing that will be done on generated triples 
                                   Currently suppots:  bulk, json-ld
-c --config CONFIG FILE	           The path to a configuration file. Every parameter can be put in its long form in the 
                                   configuration file. e.g:
                                    flinkBin=/opt/flink-1.8.0/bin/flink
                                    path=/home/rml/mapping.rml.ttl
                                   Commandline parameters override properties.
```

---

*TODO: documentation below needs updates.* 

#### Examples

##### Processing a stream

An example of how to define the generation of an RDF stream from a stream in an RML Mapping via TCP.
```
 <#TripleMap>

    a rr:TriplesMap;
    rml:logicalSource [
        rml:source [
            rdf:type rmls:TCPSocketStream ;
            rmls:hostName "localhost";
            rmls:type "PULL" ;
            rmls:port "5005"
        ];
        rml:referenceFormulation ql:JSONPath;
    ];

    rr:subjectMap [
        rml:reference "$.id";
        rr:termType rr:IRI;
        rr:class skos:Concept
    ];

    rr:predicateObjectMap [
            rr:predicateMap [
                rr:constant dcterms:title;
                rr:termType rr:IRI
            ];
            rr:objectMap [
                rml:reference "$.id";
                rr:termType rr:Literal
            ]
        ].
```
The RML Mapping above can be executed as follows:

The input and output in the RML Framework are both TCP clients when streaming. Before running stream mappings the input and output ports must be listened to by an application. For testing purposes the following commands can be used:
 ```
 nc -lk 5005 # This will start listening for input connections at port 5005
 nc -lk 9000 # This will start listening for output connections at port 9000
 # This is for testing purposes, your own application needs to start listening to the input and output ports. 
 ```
Once the input and output ports are listened to by applications or by the above commands, the RML Mapping can be executed. The RML Framework will open the input and output sockets so it can act upon data that will be written to the input socket.
```
bash run.sh -p /home/wmaroy/framework/src/main/resources/json_stream_data_mapping.ttl -s 9000
# The -p paramater sets the mapping file location
# The -s parameter sets the output socket port number
# The -o parameter sets the output path if the output needs to be written to a file instead of a stream.
```

Whenever data is written (every data object needs to end with `\r\n`) to the socket, this data will be processed by the RML Framework.

The repository contains node.js scripts for setting up stream input and output. The readme can be found in the `scripts` folder.

##### Generating a stream from a Kafka Source

An example of how to define the generation of an RDF stream from a stream in an RML Mapping via Kafka.
```
 <#TripleMap>

    a rr:TriplesMap;
    rml:logicalSource [
        rml:source [
            rdf:type rmls:KafkaStream ;
            rmls:zookeeper "zookeeper";
            rmls:broker "broker" ;
            rmls:groupid "groupid";
            rmls:topic "topic";
        ];
        rml:referenceFormulation ql:JSONPath;
    ];
```

##### Generating a stream from a file
```
<#TripleMap>

    a rr:TriplesMap;
    rml:logicalSource [
        rml:source [
            rdf:type rmls:FileStream;
            rmls:path "/home/wmaroy/github/rml-framework/akka-pipeline/src/main/resources/io/rml/framework/data/books.json"
        ];
        rml:referenceFormulation ql:JSONPath;
        rml:iterator "$.store.books[*]"
    ];

    rr:subjectMap [
        rr:template "{$.id}" ;
        rr:termType rr:IRI;
        rr:class skos:Concept
    ];

    rr:predicateObjectMap [
            rr:predicateMap [
                rr:constant dcterms:title;
                rr:termType rr:IRI
            ];
            rr:objectMap [
                rml:reference "$.id";
                rr:termType rr:Literal
            ]
        ].
```

##### Generating a dataset from a dataset

```
 <#TripleMap>

    a rr:TriplesMap;
    rml:logicalSource [
        rml:source "/home/wmaroy/github/rml-framework/akka-pipeline/src/main/resources/io/rml/framework/data/books_small.json";
        rml:referenceFormulation ql:JSONPath;
        rml:iterator "$.store.books"
    ];

    rr:subjectMap [
        rml:reference "id";
        rr:termType rr:IRI;
        rr:class skos:Concept
    ];

    rr:predicateObjectMap [
            rr:predicateMap [
                rr:constant dcterms:title;
                rr:termType rr:IRI
            ];
            rr:objectMap [
                rml:reference "id";
                rr:termType rr:Literal
            ]
        ] .
        
 ```
 
##### UML Diagram (Simplified)
 
 ![alt txt] (images/rml-stream-uml-simplified.png "Uml diagram")

#### RML Stream Vocabulary 

Namespace: <http://semweb.mmlab.be/ns/rmls#> 

The RML vocabulary have been extended with rmls to support streaming logical sources. 
The following are the classes/terms currently used:
* **rmls:[stream type]** 
    * **rmls:TCPSocketStream** specifies that the logical source will be a tcp socket stream.
    * **rmls:FileStream** specifies that the logical source will be a file stream. 
    * **rmls:KafkaStream** specifies that the logical source will be a kafka stream.
   
 
* **rmls:hostName** specifies the desired host name of the server, from where data will be streamed from.


* **rmls:port** specifies a port number for the stream mapper to connect to. 


* **rmls:type** specifies how a streamer will act: 
    * **"PULL"**:  
      The stream mapper will act as a client.  
      It will create a socket and connect to the specified port at the given host name.  
      **rmls:port** and **rmls:hostName** needs to be specified.  
    * **"PUSH"**:  
      The stream mapper will act as a server and will start listening at the given port.  
      If the given port is taken, the mapper will keep opening subsequent ports until a free port is found.    
      Only **rmls:port** needs to be specified here.  
    
Example of a valid json logical source map using all possible terms: 

```

rml:logicalSource [
        rml:source [
            rdf:type rmls:TCPSocketStream ;
            rmls:hostName "localhost";
            rmls:type "PULL" ;
            rmls:port "5005"
        ];
        rml:referenceFormulation ql:JSONPath;
    ];
```


#### Test
##### Folders

There are 4 types of test case folders:
* rml-original-testcases
* rml-testcases
* stream
    * kafka
    * tcp
* negative_test_cases
* temp_ignored_testcases 


1. rml-original-testcases contains all the original test cases without edits in the sub files/folders.  

2. rml-testcases contains all the test cases for which, the current implementation should pass. 

3. stream contains all streaming test cases which should be checked using the stream-test.sh in the root folder.
    * kafka contains all the test cases with kafka cluster as logical source.
    * tcp contains all the test cases with tcp socket as a logical source. 

4. negative_test_cases contains cases for which, the current implementation should throw exceptions/fail.  

5. temp_ignored_testcases contains cases for which, the current implementation cannot pass due to missing features. 

##### Kafka Test

You will need to first have the flink server up and running, just as you would before running the RML streamer from CLI.

##### Versioning problem
Flink supports kafka connectors, but only for their respective versions.

Ex. FlinkConsumer010/-Producer010 will use kafka clients module from version 0.10 

This prevents us from implementing a dynamic kafka version support since same modules, used by a library with different versions,
get overwritten by maven depending on it's distance in the dependency tree. 

A posssible [solution](http://jesseyates.com/2015/08/17/using-maven-shade-to-run-multiple-versions-in-a-jvm.html) to the problem, will be making an empty module using the aforementioned connectors libraries. 

We could then change the naming of the conflicting dependencies using maven-shade-plugin, and then compiling this wrapper 
module as a library to be used in the streamer. 

It should work but it's not really a solution but a workaound instead.....


###### Executing

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

###### Main Script.

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

###### What does the scripts do?


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


##### Streaming Tests

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
