#!/bin/bash

# run flink job
#echo 'Running Flink Job...'
#echo '----------------------'; echo ''

#bash ../flink/build-target/bin/flink run -c io.rml.framework.BatchJob target/framework-1.0-SNAPSHOT.jar
bash ~/Downloads/flink-1.3.2/bin/flink run  -c io.rml.framework.Main target/framework-1.0-SNAPSHOT.jar
echo ''

# view output
#echo 'Viewing output..'
#echo '------------------'; echo ''
#cat output.txt
#echo ''
