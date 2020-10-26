# README: Flink Netty Connector Snapshot

The `pom.xml` has been updated and swapped the old netty connector for the current snapshot. When using Intellij, it will ask to reload the dependencies. Click on it, after building you will see `flink-connector-netty_2.11:1.1-SNAPSHOT` will be added to the list of maven dependencies.

![](img/reload_dependencies.png)

When updating the `pom.xml` file, the icon in the top right corner will ask to reload the decencies, which in this case will update to the snapshot.

![](img/dependencies.png)

Once you build with the newly updated `pom.xml`, the snapshot version will appear in the dependency list instead of the old version.

### Testing the build on a Flink 1.11 cluster

Download [Apache Flink 1.11.2 for Scala 2.11](https://www.apache.org/dyn/closer.lua/flink/flink-1.11.2/flink-1.11.2-bin-scala_2.11.tgz) from the [Apache Flink downloads page](https://flink.apache.org/downloads.html) and untar the file.

Make sure all function files are in place (see: [documentation](https://gitlab.ilabt.imec.be/rml/proc/rml-streamer/-/blob/development/documentation/README_Functions.md)) and execute the `flink-1.11.2/bin/start-cluster.sh`  and submit your job. Once you are done running all your jobs, stop the cluster by running the `flink-1.11.2/bin/stop-cluster.sh` script.

Normally no errors should occur.