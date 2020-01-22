# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).


## Unreleased

* Function mapping.
* Joins of data streams
* Optimal usage of the task slots assigned to a task manager/job.

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

[1.0.0]: https://gitlab.ilabt.imec.be/rml/proc/rml-streamer/-/tags/v1.0.0
[1.1.0]: https://gitlab.ilabt.imec.be/rml/proc/rml-streamer/compare/v1.0.0...v1.1.0
[1.2.0]: https://gitlab.ilabt.imec.be/rml/proc/rml-streamer/compare/v1.1.0...v1.2.0  
[1.2.1]: https://gitlab.ilabt.imec.be/rml/proc/rml-streamer/compare/v1.2.0...v1.2.1  