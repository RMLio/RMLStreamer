FROM openjdk

RUN apt-get update
RUN apt-get install curl -y
WORKDIR /
RUN curl -L https://archive.apache.org/dist/flink/flink-1.3.2/flink-1.3.2-bin-hadoop2-scala_2.10.tgz > flink.tgz
RUN mkdir flink
RUN tar xzf flink.tgz
ADD target/framework-1.0-SNAPSHOT.jar /streamer.jar
ADD docker/* /
WORKDIR /flink-1.3.2
RUN mv /json-mapreduce-1.0.jar ./lib
RUN mv /flink-conf.yaml ./conf
WORKDIR /
CMD /flink-1.3.2/bin/start-local.sh > /dev/null; bash run.sh -p /data/mapping.rml.ttl -o /data/output.nt > /dev/null; chmod 777 /data/output.nt
