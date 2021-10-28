# README: Functions


When deploying and running jobs on Flink, make sure
- to either place the external jars (e.g. `IDLabFunctions.jar` and `GrelFunctions.jar`) in Flink's `lib` directory,
or package the RMLStreamer along with those jars by placing them in `src/main/resources`.  
- to place the following files in the directory where the `flink run ...` command is issued.
These files can be obtained from `src/main/resources`:
    - `functions_grel.ttl`
    - `functions_grel.ttl`
    - `grel_java_mapping.ttl`
    - `idlab_java_mapping.ttl`
---

## Example: RML Streamer + Flink    
Flink's `lib` directory should contain the jar-files with the custom functions. In this example, these are marked with `*`
```
flink-1.14.0-scala_2.11
    └── lib
        ├── GrelFunctions.jar                       *
        ├── IDLabFunctions.jar                      *
        ├── flink-dist_2.11-1.14.0.jar
        ├── flink-table-blink_2.11-1.14.0.jar
        └── flink-table_2.11-1.14.0.jar
``` 
When running the RML Streamer on Flink, the directory should look like
```
.
├── RMLStreamer-2.2.0.jar
├── functions_grel.ttl
├── functions_idlab.ttl
├── grel_java_mapping.ttl
├── idlab_java_mapping.ttl
├── mapping.ttl
└── input_data.csv
```
Note that the function descriptions and function mappings are present.

The command for running the RML Streamer on Flink should look like
```
~/flink/flink-1.14.0-scala_2.11/bin/flink run -c io.rml.framework.Main RMLStreamer-2.2.0.jar toFile --output-path $(pwd)'/out.ttl' -m mapping.ttl
```       
       
## Test Cases
### FnO Tests
The official FnO testcases that are working can be found at `test/resources/fno-testcases`. 

### SandboxTests
> These tests are still experimental.

The resources can be found at `test/resources/sandbox/function_related` and are executed from `io.rml.framework.SandboxTests`.
Every test output is compared to the RMLMapper's output (e.g. `output.ttl`) for that test. A test passes when its output is equal to the RMLMapper's output.
Tests marked as `pending` should be considered as not working.<br>

The sandbox testcases are
- `condition-function-on-po`: uses the functions
    - `idlab-fn:trueCondition`
    - `idlab-fn:stringContainsOtherString` 
    - `grel:toUpperCase`
  
- `condition-on-mapping-subject-function`: uses the functions
    - `idlab-fn:trueCondition`
    - `idlab-fn:notEqual` 

- `condition-on-po`: uses the functions
    - `idlab-fn:trueCondition`
    - `idlab-fn:equal` 
    

- `contains` uses function  `grel:string_contains`

- `controls_if_contains`: uses the functions
    - `grel:controls_if`
    - `grel:string_contains`

- `controls_if_true`: uses the functions `grel:controls_if`. 
    
- `controls_if_false`: uses the functions `grel:controls_if`
    
- `equal`: uses function `idlab-fn:equal`

- `notEqual`: uses function `idlab-fn:notEqual`

- `using_trueCondition_and_contains`
    - `idlab-fn:trueCondition`
    - `grel:string_contains` 
    
- `using_trueCondition_and_equal`
    - `idlab-fn:trueCondition`
    - `idlab-fn:equal` 




## Tutorial: Using a function from a local JAR

The following FnO-testcases use the function `toUpperCaseURL` 
- RMLFNOTC0004-CSV
- RMLFNOTC0005-CSV
- RMLFNOTC0006-CSV

The following steps show how to integrate the `toUpperCaseURL` function in the RML Streamer.

### Step 1: creating the JAR
This step is based on the best-practice example [`grel-functions-java`](https://github.com/FnOio/grel-functions-java).
- Create package  `io.fno.idlab` and within that package, create the class `IDLabFunctions`
- For these testcases, we need a function that returns the given URL in uppercase. 
- Make sure to set the Maven compiler to a version compatible with the RMLStreamer's version.

The following listing serves a minimalistic example that shows a possible implementation of the `toUpperCaseURL`-function.   
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
An FnO description represents the abstract definition of a function.<br> 
The aforementioned testcases require a function that returns a valid URL in uppercase.
 Its description is shown in the following listing, and can be found in `functions_idlab.ttl`.  

```Turtle
idlab-fn:toUpperCaseURL
    a                   fno:Function ;
    fno:name            "toUppercaseURL" ;
    rdfs:label          "toUppercaseURL" ;
    dcterms:description "Returns an uppercase, valid url." ;
    fno:solves          grel:prob_ucase ;
    fno:expects         ( idlab-fn:_str ) ;
    fno:returns         ( idlab-fn:_stringOut ) .
```

### Step 3: map FnO descriptions to the corresponding implementations
In the previous step, the abstract functions were created. 
The current step will define the link between abstract function descriptions and the corresponding implementation.
 This is illustrated by the following listing, extracted from `idlab_java_mapping.ttl`.
```Turtle 
grelm:IDLabFunctions
    a                  fnoi:JavaClass ;
    doap:download-page "IDLabFunctions.jar" ;
    fnoi:class-name    "io.fno.idlab.IDLabFunctions" .


grelm:uppercaseURLMapping
    a                    fno:Mapping ;
    fno:function         idlab-fn:toUpperCaseURL;
    fno:implementation   grelm:IDLabFunctions ;
    fno:parameterMapping [ ] ;
    fno:returnMapping    [ ] ;
    fno:methodMapping    [ a                fnom:StringMethodMapping ;
                           fnom:method-name "toUpperCaseURL" ] ;
.

```
This mapping instructs the RML Streamer to look for a method called `toUpperCaseURL` within the `io.fno.idlab.IDLabFunctions`-class of the `IDLabFunctions.jar`. Make sure the JAR-file is located in `main/resources`.

## How the `FunctionLoader` works

The function descriptions and mappings mentioned in the previous steps will be used by the `FunctionLoader`.

First, a `FunctionLoader` has to be aware of the available functions. 
Therefore, it can be instantiated providing file paths to the function description files. 
When no such file paths are provided, the default function descriptions are used (i.e. `functions_grel.ttl`). 

Secondly, function URIs are mapped to the corresponding implementations by parsing the function mappings
 (e.g. `resources/grel_java_mapping.ttl` and `resources/idlab_java_mapping.ttl`). 
 During this step, every function URI is mapped to a `FunctionMetaData`-object which contains the necessary metadata  such as: the *download-page* of the library, the *class-name* of the function, the *method-name*, *input parameters* and *output parameters*.


## How the `FunctionLoader` is used

Initially, the `FunctionLoader` is used to read and parse function descriptions and mappings. 
Afterwards, when running FNOT-testcases, the `FunctionLoader`-instance is used by `io.rml.framework.engine.statement.FunctionMapGeneratorAssembler` 
to load and bind every function as specified in the testcase's `mapping.ttl`. 



# Remarks
- When the RMLStreamer is unable to find a function description or function mapping, bind method parameters to values, it will be logged as an error to the console
  and the function will not be applied.
 