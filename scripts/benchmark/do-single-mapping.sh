#!/bin/bash

ID=$1
DATA_FOLDER=$2
MEASUREMENTS_FILE=$3

STARTIME=$(($(date +%s%N)/1000000))

docker run --rm -v $DATA_FOLDER:/data rml-streamer

STOPTIME=$(($(date +%s%N)/1000000))
TIMEDIFFERENCE=`expr $STOPTIME - $STARTIME`

echo "$1,$TIMEDIFFERENCE" >> $MEASUREMENTS_FILE
