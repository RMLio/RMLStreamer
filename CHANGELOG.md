# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## Unreleased
                       
* Support for stream partitioning in windows
* Joins of data streams

## [2.1.1] - 2020-05-19

### Added
* Support for using Web of Things descriptions in logical soure and logical target, as described in [Van Assche et al](https://link.springer.com/chapter/10.1007/978-3-030-74296-6_26)
  and [Target in RML specification](https://rml.io/specs/rml-target).
  The current imlementation is a proof-of-concept. As WoT data source RMLStreamer supports MQTT streams;
  as logical target a file dump is supported.

### Changed
* Updated JsonSurfer from version 1.5.1 to 1.6.0
* Updated Flink from version 1.11.3 to 1.12.3

### Fixed
* Function loading didn't always work because the `toString` method was called on the `Uri` to a function instead of
the `value` method. (Internal [issue #132](https://gitlab.ilabt.imec.be/rml/proc/rml-streamer/-/issues/132)).

## [2.1.0] - 2020-03-18

### Added
* Support for functions on a per-record basis using the [Function Ontology](https://fno.io/).
* Web of Things source (MQTT)

### Changed
* Updated Flink from version 1.10.0 to 1.11.3
* Updated Kafka from version 2.2.2 to 2.4.1 (more versions supported using the universal connector)
* Updated VTD-XML from version 2.11 to 2.13.4

### Removed
* Drop support for Java 8, only Java 11 supported.
* TCP PUSH support disabled: this code relies on development version of Apache Bahir.

### Fixed
* Cyclic reference of parent triples maps leads to a stack overflow error (GitHub [issue #19](https://github.com/RMLio/RMLStreamer/issues/19), Internal [issue #108](https://gitlab.ilabt.imec.be/rml/proc/rml-streamer/-/issues/108))
* In some cases not all triples maps were applied when joins (static-static and static-streams) are involved (fixed together with issue above).
* Writing to file when input streams are involved is now possible (See GitHub [issue #8](https://github.com/RMLio/RMLStreamer/issues/8), internal [issue #107](https://gitlab.ilabt.imec.be/rml/proc/rml-streamer/-/issues/107)).
* XML/XPath handling was erroneous (See GitHub [issue #24](https://github.com/RMLio/RMLStreamer/issues/24), internal [issue #124](https://github.com/RMLio/RMLStreamer/issues/24)).

## [2.0.0] - 2020-06-08

### Changed
* Improved parameter handling, using [scopt](https://github.com/scopt/scopt). Not compatible with previous CLI.
* Parallelising over the (local) task slots per task manager is enabled by default.
* Checkpointing is disabled by default.
* Updated Flink from version 1.9.1 to 1.10.0
* Updated Kafka support from version 2.2.0 to 2.2.2
* Updated documentation

### Fixed
* Escape characters were not escaped in generated literals (Internal [issue #81](https://gitlab.ilabt.imec.be/rml/proc/rml-streamer/issues/81)).
* Curly brackets were not escaped correctly (Internal [issue #65](https://gitlab.ilabt.imec.be/rml/proc/rml-streamer/-/issues/65)).

## [1.2.3] - 2020-03-16

### Changed
* Updated documentation

### Fixed
* `baseIRI` parameter in `run.sh` was not correctly passed to the RMLStreamer application.
* `socket` parameter only allowed to set the port number, and output was assumed to go to `localhost`. Now you have to set `host:port` as value.

## [1.2.2] - 2020-03-03

### Added

* Option to switch on optimal usage of the task slots assigned to a task manager/job,
at the cost of losing strict output order.

### Changed
* Refactoring of tests.
* Test results are compared with expected output by comparing on RDF level, not String comparison.

### Fixed
* Because generated output is parsed as Turtle during tests, tests generating N-Quads always fail.
* A `@base` directive in the *mapping file* was used as `base IRI` when generating RDF. This is wrong; now the `base IRI`
can be set with the program argument `--baseIRI`.
* An URI with a scheme other than `http` (e.g. `tel`) was not concidered correct.

## [1.2.1] - 2020-01-21

### Added
* Instructions on how to run on Docker.

### Changed
* Bump Flink from version 1.9 to version 1.9.1.
* Bump Kafka support from verison 0.10 to any version supported by Flink's
[universal Kafka connector](https://ci.apache.org/projects/flink/flink-docs-release-1.9/dev/connectors/kafka.html#kafka-100-connector) (1.0.0+).
* Revised and refactored tests. Now only one Flink / TCP server / Kafka instance will run during test suite.

### Fixed
* Bugfix: when having more than one triples map using the same XML source, things might go wrong.

### Removed
* Kafka 0.10 not supported anymore.


## [1.2.0] - 2019-11-05

### Added
* Support for join between static and streaming data; the "parent" is the static data set, the "child" is the data stream.
* You can name the job (--job-name).
* JSON-LD as output format (--post-process json-ld).
* Option to output every triple coming from one message at once (--post-process bulk).
* Support for Kafka 0.10.

### Changed
* Bump version of Flink from 1.8.0 to 1.9.

### Removed
* Kafka 0.9 or earlier not supported anymore.
* Removed rmls:zookeeper statements from mappings since Kafka 0.10 and higher don't need it.


## [1.1.0] - 2019-05-03

### Changed
* At-least-once delivery of triples.
* Bump Flink from version 1.7.2 to 1.8.0

## [1.0.0] - 2019-03-29

### Added
* Everything! (initial release)

[1.0.0]: https://github.com/RMLio/RMLStreamer/releases/tag/v1.0.0
[1.1.0]: https://github.com/RMLio/RMLStreamer/compare/v1.0.0...v1.1.0
[1.2.0]: https://github.com/RMLio/RMLStreamer/compare/v1.1.0...v1.2.0  
[1.2.1]: https://github.com/RMLio/RMLStreamer/compare/v1.2.0...v1.2.1
[1.2.2]: https://github.com/RMLio/RMLStreamer/compare/v1.2.1...v1.2.2  
[1.2.3]: https://github.com/RMLio/RMLStreamer/compare/v1.2.2...v1.2.3
[2.0.0]: https://github.com/RMLio/RMLStreamer/compare/v1.2.3...v2.0.0  
[2.1.0]: https://github.com/RMLio/RMLStreamer/compare/v2.0.0...v2.1.0  
[2.1.1]: https://github.com/RMLio/RMLStreamer/compare/v2.1.0...v2.1.1  
