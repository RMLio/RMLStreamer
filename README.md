## RML Framework

### Installing Flink
The RML Framework runs it's jobs on Flink clusters. More information on how to install Flink and getting clusters up and running can be found [here](https://ci.apache.org/projects/flink/flink-docs-release-1.4/quickstart/setup_quickstart.html).
 At least a local cluster must be running in order to start executing RML Mappings with the RML Framework.

### Installing RML Framework
```
git clone ssh://git@git.datasciencelab.ugent.be:4444/rml/rml-streamer.git
```
Location of the Flink installation directory must be configured in `configuration.properties` .

### Executing RML Mappings

```
bash run.sh [ -p <RML Mapping Location> -o <File Output Location> -s <Output Socket Port Number> ]
```

#### Stream Mappings Examples

##### Generating a stream from a stream

An example of how to define the generation of stream from a stream in an RML Mapping.
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

The input and output in the RML Framework are both TCP clients when streaming. Before running stream mappings the input and output sockets must be opened by an application. For example:
 ```
 nc -lk 5005 # This will be the input socket
 nc -lk 9000 # This will be the output socket
 ```
Once the input and output sockets are opened by applications or by the above commands, the RML Mapping can be executed.
```
bash run.sh -p /home/wmaroy/framework/src/main/resources/json_stream_data_mapping.ttl -s 9000
# The -p paramater sets the mapping file location
# The -s parameter sets the output socket port number
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

#### Dataset Mappings Examples

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