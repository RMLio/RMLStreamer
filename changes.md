This document lists the changes for every version.

## 1.2.0

* Added support for join between static and streaming data; the "parent" is the static data set, the "child" is the data stream.
* You can name the job (--job-name)
* Added JSON-LD as output format (--post-process json-ld)
* Added option to output every triple coming from one message at once (--post-process bulk)
* Bump version of Flink to 1.9
* Dropped support for Kafka 0.9 or earlier
* Remove rmls:zookeeper statements from mappings since Kafka 0.10 and higher don't need it

## 1.2.1
* Bump Flink to version 1.9.1.
* Bump Kafka to version 2.2.0, only use universal Kafka connector.
* Bugfix: when having more than one triples map using the same XML source, things might go wrong.
* Revised and refactored tests. Now only one Flink / TCP server / Kafka will run during test suite.