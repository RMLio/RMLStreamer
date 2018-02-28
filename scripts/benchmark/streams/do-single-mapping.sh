#!/bin/bash

ID=$1
DATA_FOLDER=$2
INPUT_FILE_STREAM=$3
DELAY=$4
PREFIX_URL=$5
SUFFIX=$6
AMOUNT_TRIPLES=$7
MEASUREMENTS_FILE=$8

docker run --name stream-in -d --net=host -v $DATA_FOLDER:/data stream-scripts streamFromFile.js /data/$INPUT_FILE_STREAM 5005 $DELAY $SUFFIX /data/in.csv > /dev/null
docker run --name stream-out -d --net=host -v $DATA_FOLDER:/data stream-scripts processOutput.js 9000 /data/out.csv $PREFIX_URL $AMOUNT_TRIPLES > /dev/null

STARTIME=$(($(date +%s%N)/1000000))

docker run --rm --net=host -v $DATA_FOLDER:/data rml-streamer-stream

STOPTIME=$(($(date +%s%N)/1000000))
TIMEDIFFERENCE=`expr $STOPTIME - $STARTIME`

cat $DATA_FOLDER/in.csv >> $DATA_FOLDER/merge.csv
cat $DATA_FOLDER/out.csv >> $DATA_FOLDER/merge.csv
sort $DATA_FOLDER/merge.csv > $DATA_FOLDER/temp.csv
echo "id,time" | cat - $DATA_FOLDER/temp.csv > $DATA_FOLDER/merge.csv

DELAY=`docker run --rm -v $DATA_FOLDER:/data stream-scripts parseTimes.js /data/merge.csv /data/compare.csv`

echo "$1,$TIMEDIFFERENCE,$DELAY" >> $MEASUREMENTS_FILE

docker stop stream-out > /dev/null
docker rm stream-out stream-in > /dev/null
