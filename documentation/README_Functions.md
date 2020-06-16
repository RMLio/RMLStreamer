# Tutorial
> Under construction

## Using a function from a local JAR

The following testcases use the function `toUpperCaseURL`, which isn’t implemented yet: 

- RMLFNOTC0004-CSV
- RMLFNOTC0005-CSV
- RMLFNOTC0006-CSV

The following steps show how to integrate the `toUpperCaseURL` function in the RML Streamer.

### Step 1: creating the JAR
This step is based on best-practice example `grel-functions-java`.
- Create package  `io.fno.idlab` and within that package, create the class `IDLabFunctions`
- For these testcases, we need a function that returns the given URL in uppercase. 

```Java
package io.fno.idlab;

public class IDLabFunctions {
    public static String toUpperCaseURL(String s) {
        if(!s.startsWith("http"))
            return "http://" + s.toUpperCase();
        return s.toUpperCase();
    }
}
```

Use Maven to build a JAR-file, and move this JAR-file to the RMLStreamer’s `main/resources`.

### Step 2: defining the FnO descriptions
An FnO description represents the abstract definition of a function. 

The testcases mentioned above require a function that returns a valid URL in uppercase, and is described as follows


```Turtle
# source:  `functions_idlab.ttl`.
grel:prob_ucase
    a                   fno:Problem ;
    fno:name            "The ucase problem"^^xsd:string ;
    dcterms:description "Converting a string to upper case characters."^^xsd:string .

grel:toUpperCaseURL
    a                   fno:Function ;
    fno:name            "toUppercaseURL" ;
    rdfs:label          "toUppercaseURL" ;
    dcterms:description "Returns an uppercase, valid url." ;
    fno:solves          grel:prob_ucase ;
    fno:expects         ( grel:valueParam ) ;
    fno:returns         ( grel:stringOut ) .
```

### Step 3: map FnO descriptions to the corresponding implementations
In the previous step, the abstract functions were created. 
The current step will define the link between abstract function descriptions and the corresponding implementation. This is illustrated by the following snippet.
```Turtle 
grelm:IDLabFunctions
    a                  fnoi:JavaClass ;
    doap:download-page "IDLabFunctions.jar" ;
    fnoi:class-name    "io.fno.idlab.IDLabFunctions" .

#UPPERCASERURL
grelm:uppercaseURLMapping
    a                    fno:Mapping ;
    fno:function         grel:toUpperCaseURL ;
    fno:implementation   grelm:IDLabFunctions ;
    fno:parameterMapping [ ] ;
    fno:returnMapping    [ ] ;
    fno:methodMapping    [ 
        a   fnom:StringMethodMapping ;
            fnom:method-name "toUpperCaseURL" 
    ] ;
.
```
This mapping instructs the RML Streamer to look for a method called `toUpperCaseURL` within the `io.fno.idlab.IDLabFunctions`-class of the `IDLabFunctions.jar`. Make sure that the JAR-file is located in `main/resources`.

## How the `FunctionLoader` works

The function descriptions and mappings mentioned in the previous steps will be used by the `FunctionLoader`.

First, a `FunctionLoader` has to be aware of the available functions. 
Therefore, it can be instantiated providing file paths to the function description files. When no such file paths where provided, the default function descriptions are used (e.g. `functions_grel.ttl`). 

Secondly, function URIs are mapped to the corresponding implementations by parsing the function mappings (e.g. `resources/grel_java_mapping.ttl` and `resources/idlab_java_mapping.ttl`). During this step, every function URI is mapped to a `FunctionMetaData`-object which contains the necessary meta data  such as: the *download-page* of the library, the *class-name* of the function, the *method-name*, *input parameters* and *output parameters*.


## How the `FunctionLoader` is used

Initially, the `FunctionLoader` is used to read and parse function descriptions and mappings. Afterwards, when running FNOT-testcases, the `FunctionLoader`-instance is used to load and bind every function as specified in the `mapping.ttl`. 


## Remarks
- When running the `SandboxTests` , make sure to set the *Working Directory* of the *Run-configuration* to the absolute path of `rml-streamer/src/test/resources/sandbox`.


