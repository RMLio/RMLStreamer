# README: Functions

## Built-in functions

Two function libraries are supported out of the box in RMLStreamer:
* GREL functions (<https://github.com/FnOio/grel-functions-java>)
* IDLab functions (<https://github.com/FnOio/idlab-functions-java>)

No extra configuration or parameters need to be provided to use those.

Check out [src/test/resources/fno-testcases](../src/test/resources/fno-testcases) for example RML Mappings.

## Using other function libraries

RMLStreamer can also execute functions from other libraries.
To use functions, three things have to be provided:

* A JAR file containing the code to execute functions;
* Function descriptions: a document describing the functions in the library semantically using [FnO](https://fno.io/).
* Implementation mappings: a document mapping the functions from the function descriptions
  to the implementation in the JAR file, also using FnO.

Often the function descriptions an the implementation mappings are bundled in one document.

How to do this is explained as an example in the [Function Agent](https://github.com/FnOio/function-agent-java#example)
library, which is used by RMLStreamer to handle functions.

A minimal example is provided in the tests: [src/test/resources/sandbox/function_related/external_jar/](../src/test/resources/sandbox/function_related/external_jar)

If there are a JAR file and FnO descriptions, RMLStreamer can be invoked with the `-f` or `--function-descriptions`
parameter, like so:

```shell
$ FLINK_BIN run [Flink options] -c io.rml.framework.Main <path to RMLStreamer jar> \
  toFile \
  --output-path /tmp/helloworld.nt \
  --mapping-file /<path to RMLStreamer root>/src/test/resources/sandbox/function_related/external_jar/mapping.ttl \
  --function-descriptions /<path to RMLStreamer root>/src/test/resources/sandbox/function_related/external_jar/simple-test-function-fno.ttl
```

Notes
- The paths to funtion descriptions or JAR files can also be URLs;
- One can pass multiple function description locations to the `--function-descriptions` parameter separated by a space.

## Test Cases
### FnO Tests
The official FnO testcases that are working can be found at [src/test/resources/fno-testcases](../src/test/resources/fno-testcases). 

### SandboxTests
> These tests are still experimental.

The resources can be found at [src/test/resources/sandbox/function_related](../src/test/resources/sandbox/function_related)
and are executed from `io.rml.framework.SandboxTests`.
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

- `external_jar`: uses the `HelloWorld` function from an external JAR file.

- `notEqual`: uses function `idlab-fn:notEqual`

- `using_trueCondition_and_contains`
    - `idlab-fn:trueCondition`
    - `grel:string_contains` 
    
- `using_trueCondition_and_equal`
    - `idlab-fn:trueCondition`
    - `idlab-fn:equal` 

# Remarks
- When the RMLStreamer is unable to find a function description or function mapping, bind method parameters to values, it will be logged as a warning to the console
  and the function will not be applied.
 