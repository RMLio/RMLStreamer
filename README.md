## RMLStreamer
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.3887065.svg)](https://doi.org/10.5281/zenodo.3887065)

The RMLStreamer generates [RDF](https://www.w3.org/2001/sw/wiki/RDF) from files or data streams
using [RML](http://rml.io/). The difference with other RML implementations is that it can handle
*big* input files and *continuous data streams*, like sensor data.

Documentation regarding the use of (custom) functions can be found [here](documentation/README_Functions.md).

### Quick start (standalone)

* Download `RMLStreamer-<version>-standalone.jar` from the [latest release](https://github.com/RMLio/RMLStreamer/releases/latest).
* Run it as
```
$ java -jar RMLStreamer-<version>-standalone.jar <commands and options>
```

See [Basic commands](#basic-commands) (where you replace `$FLINK_BIN run <path to RMLStreamer jar>` with `java -jar RMLStreamer-<version>-standalone.jar`)
and [Complete RMLStreamer usage](#complete-rmlstreamer-usage) for
examples, possible commands and options.

### Moderately quick start (Docker)

If you want to get the RMLStreamer up and running within 5 minutes using Docker, check out [docker/README.md](docker/README.md)

### Not so quick start (deploying on a cluster)

If you want to deploy it yourself, read on.

If you want to develop, read [these instructions](documentation/README_DEVELOPMENT.md).

### Installing Flink
RMLStreamer runs its jobs on Flink clusters.
More information on how to install Flink and getting started can be found [here](https://ci.apache.org/projects/flink/flink-docs-release-1.14/try-flink/local_installation.html).
At least a local cluster must be running in order to start executing RML Mappings with RMLStreamer.
Please note that this version works with Flink 1.14.4 with Scala 2.11 support, which can be downloaded [here](https://archive.apache.org/dist/flink/flink-1.14.4/flink-1.14.4-bin-scala_2.11.tgz).

### Grabbing RMLStreamer...

Download `RMLStreamer-<version>.jar` from the [latest release](https://github.com/RMLio/RMLStreamer/releases/latest).

### ... or building RMLStreamer

In order to build a jar file that can be deployed on a Flink cluster, you need:
- a Java JDK >= 11 and <= 13 (We develop and test on JDK 11)
- Apache Maven 3 or higher

Clone or download and then build the code in this repository:

```
$ git clone https://github.com/RMLio/RMLStreamer.git 
$ cd RMLStreamer
```
and then run:
```
$ mvn -DskipTests clean package
```

`-DskipTests` just builds and packages without running tests. If you want to run the tests, just omit this parameter.

`clean` cleans any cached builds before packaging. While not strictly necessary, it is considered good practice to do
so.

The resulting `RMLStreamer-<version>.jar`, found in the `target` folder, can be deployed on a Flink cluster.

**Note**: To build a *stand-alone* RMLStreamer jar, add `-P 'stand-alone'` to the build command, e.g.:
```
$ mvn clean package -DskipTests -P 'stand-alone'
```

### Executing RML Mappings

*This section assumes the use of a CLI. If you want to use Flink's web interface, check out
[this section](docker/README.md#3-deploy-rmlstreamer-using-the-flink-web-interface) in the Docker README.*

Here we give examples for running RMLStreamer from the command line. We use `FLINK_BIN` to denote the Flink CLI tool,
usually found in the `bin` directory of the Flink installation. E.g. `/home/myuser/flink-1.14.0/bin/flink`.
For Windows a `flink.bat` script is provided.

The general usage is:

```
$ FLINK_BIN run [Flink options] -c io.rml.framework.Main <path to RMLStreamer jar> [toFile|toKafka|toTCPSocket] [options]
```

`FLINK HOME`  | The path to the provided Flink CLI script.
Flink options | Options to the Flink run script. Example: `-p 4` sets the `parallelism` to 4.
`-c io.rml.framework.Main` | This is the application class of RMLStreamer.
Path to RMLStreamer jar | The absolute path to the RMLStreamer jar file.
RMLStreamer options | The actual program arguments for RMLStreamer. See below for a full list.

#### Basic commands:
```shell script
# write output to file(s)
$FLINK_BIN run <path to RMLStreamer jar> toFile --mapping-file <path to mapping file> --output-path <path to output file>  

# write output to a listening socket (only if logical source(s) are streams)
$FLINK_BIN run <path to RMLStreamer jar> toTCPSocket --output-socket <host:port>

# write output to kafka topic (only if logical source(s) are streams)
$FLINK_BIN run <path to RMLStreamer jar> toKafka --broker-list <host:port> --topic <topic name>
```

#### Complete RMLStreamer usage:

```
Usage: RMLStreamer [toFile|toKafka|toTCPSocket|noOutput] [options]

  -f, --function-descriptions <function description location 1>,<function description location 2>...
                           An optional list of paths to function description files (in RDF using FnO). A path can be a file location or a URL.
  -j, --job-name <job name>
                           The name to assign to the job on the Flink cluster. Put some semantics in here ;)
  -i, --base-iri <base IRI>
                           The base IRI as defined in the R2RML spec.
  --disable-local-parallel
                           By default input records are spread over the available task slots within a task manager to optimise parallel processing,at the cost of losing the order of the records throughout the process. This option disables this behaviour to guarantee that the output order is the same as the input order.
  -m, --mapping-file <RML mapping file>
                           REQUIRED. The path to an RML mapping file. The path must be accessible on the Flink cluster.
  --json-ld                Write the output as JSON-LD instead of N-Quads. An object contains all RDF generated from one input record. Note: this is slower than using the default N-Quads format.
  --bulk                   Write all triples generated from one input record at once, instead of writing triples the moment they are generated.
  --checkpoint-interval <time (ms)>
                           If given, Flink's checkpointing is enabled with the given interval. If not given, checkpointing is enabled when writing to a file (this is required to use the flink StreamingFileSink). Otherwise, checkpointing is disabled.
Command: toFile [options]
Write output to file 
Note: when the mapping consists only of stream triple maps, a StreamingFileSink is used. This sink will write the output to a part file at every checkpoint.
  -o, --output-path <output file>
                           The path to an output file. Note: when a StreamingFileSink is used (the mapping consists only of stream triple maps), this path specifies a directory and optionally an extension. Part files will be written to the given directory and the given extension will be used for each part file.
Command: toKafka [options]
Write output to a Kafka topic
  -b, --broker-list <host:port>[,<host:port>]...
                           A comma separated list of Kafka brokers.
  -t, --topic <topic name>
                           The name of the Kafka topic to write output to.
  --partition-id <id>      EXPERIMENTAL. The partition id of kafka topic to which the output will be written to.
Command: toTCPSocket [options]
Write output to a TCP socket
  -s, --output-socket <host:port>
                           The TCP socket to write to.
```

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
$ nc -lk 5005 # This will start listening for input connections at port 5005
$ nc -lk 9000 # This will start listening for output connections at port 9000
 # This is for testing purposes, your own application needs to start listening to the input and output ports. 
 ```
Once the input and output ports are listened to by applications or by the above commands, the RML Mapping can be executed. RMLStreamer will open the input and output sockets so it can act upon data that will be written to the input socket.
```
$FLINK_BIN run <path to RMLStreamer jar> toTCPSocket -s localhost:9000 -m .../framework/src/main/resources/json_stream_data_mapping.ttl
# The -m paramater sets the mapping file location
# The -s parameter sets the output socket port number
```

Whenever data is written (every data object needs to end with `\n`) to the socket, this data will be processed by the RML Framework.

##### Generating a stream from a Kafka Source

An example of how to define the generation of an RDF stream from a stream in an RML Mapping via Kafka.
```
 <#TripleMap>

    a rr:TriplesMap;
    rml:logicalSource [
        rml:source [
            rdf:type rmls:KafkaStream ;
            rmls:broker "localhost:9092" ;
            rmls:groupId "groupId";
            rmls:topic "topic";
        ];
        rml:referenceFormulation ql:JSONPath;
    ];
```

**Note on using Kafka with Flink**: As a consumer, the Flink Kafka client never *subscribes* to a topic, but it is
*assigned* to a topic/partition (even if you declare it to be in a *consumer group* with the `rmls:groupId` predicate). This means that it doesn't do
anything with the concept *"consumer group"*, except for committing offsets. This means that load is not spread across
RMLStreamer jobs running in the same consumer group. Instead, each RMLStreamer job is assigned a partition. 
This has some consequences:
* When you add multiple RMLStreamer jobs in a consumer group, and the topic it listens to has one partition,
only one instance will get the input.
* If there are multiple partitions in the topic and multiple RMLStreamer jobs, it could be that two (or more) jobs
are assigned a certain partition, resulting in duplicate output.

See also https://stackoverflow.com/questions/38639019/flink-kafka-consumer-groupid-not-working .

The only option for spreading load is to use multiple topics, and assign one RMLStreamer job to one topic.

##### Generating a stream from a dataset

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
 
#### RML Stream Vocabulary (non-normative)

Namespace: <http://semweb.mmlab.be/ns/rmls#> 

The RML vocabulary have been extended with rmls to support streaming logical sources. 
The following are the classes/terms currently used:
* **rmls:[stream type]** 
    * **rmls:TCPSocketStream** specifies that the logical source will be a tcp socket stream.
    * **rmls:FileStream** specifies that the logical source will be a file stream (to be implemented). 
    * **rmls:KafkaStream** specifies that the logical source will be a kafka stream.
   
 
* **rmls:hostName** specifies the desired host name of the server, from where data will be streamed from.


* **rmls:port** specifies a port number for the stream mapper to connect to. 
    
Example of a valid json logical source map using all possible terms: 

```

rml:logicalSource [
        rml:source [
            rdf:type rmls:TCPSocketStream ;
            rmls:hostName "localhost";
            rmls:port "5005"
        ];
        rml:referenceFormulation ql:JSONPath;
    ];
```

### Logging

RMLStreamer uses Flink's Log4j 2 system. It can be configured in 
`$FLINK_HOME/conf/log4j.properties`.

To adjust the log level for RMLStreamer specifically, add two lines like this:

```properties
logger.rmlstreamer.name = io.rml.framework
logger.rmlstreamer.level = DEBUG

```