This document lists the changes for every version.

## 1.1.1

* You can name the job (--job-name)
* Added JSON-LD as output format (--post-process json-ld)
* Added option to output every triple coming from one message at once (--post-process bulk)
* Bump version of Flink to 1.9
* Dropped support for Kafka 0.9 or earlier
* Remove rmls:zookeeper statements from mappings since Kafka 0.10 and higher don't need it  