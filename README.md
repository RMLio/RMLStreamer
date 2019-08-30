## RML Framework

### Installing Flink
The RML Streamer runs its jobs on Flink clusters.
More information on how to install Flink and getting started can be found [here](https://ci.apache.org/projects/flink/flink-docs-release-1.7/tutorials/local_setup.html).
At least a local cluster must be running in order to start executing RML Mappings with the RML Streamer.
It is not necessary to install Hadoop. Please note that the current repository works with Flink 1.9.0 with Scala 2.11 support, which can be downloaded [here](https://www.apache.org/dyn/closer.lua/flink/flink-1.9.0/flink-1.9.0-bin-scala_2.11.tgz).

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
-t --kafkaTopic                    The kafka topic to which the output will be streamed to. 
--pp --post-process                 The name of the post processing that will be done on generated triples 
                                   Default is: None
                                   Currently supports:  "bulk", "json-ld"

-a --parallelism                   The parallelism to assign to the job. The default is 1.
-t --kafkaTopic                    The kafka topic to which the output will be streamed to. 
--pp --post-process                 The name of the post processing that will be done on generated triples 
                                   Default is: None
                                   Currently supports:  "bulk", "json-ld"
--pi --partition-id                The partition id of kafka topic to which the output will be written to. 
                                   Required for "--partition-type fix"
--pt --partition-type              The type of the partitioner which will be used to partition the output
                                   Default is: flink's default partitioner
                                   Currently supports: "fixed", "kafka", "default"  
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
