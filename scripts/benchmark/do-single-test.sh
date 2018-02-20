#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

IFS=',' read -ra ADDR <<< "$1"
ID="${ADDR[0]}"
INPUT_FILE_PATH="${ADDR[3]}"
MAPPING_FILE_PATH="${ADDR[4]}"

MEASUREMENTS_FILE=$2

UUID=$(uuidgen)
TEMP_FOLDER=$(pwd)"/tmp"

mkdir -p $TEMP_FOLDER/$UUID
cp $INPUT_FILE_PATH $TEMP_FOLDER/$UUID
cp $MAPPING_FILE_PATH $TEMP_FOLDER/$UUID

$DIR/do-single-mapping.sh $ID $TEMP_FOLDER/$UUID $MEASUREMENTS_FILE

rm -rf $TEMP_FOLDER/$UUID
