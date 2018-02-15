#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
TEST_FILE=$1
MEASUREMENTS_FILE=$2

header=true

while IFS='' read -r line || [[ -n "$line" ]]; do
    if [ "$header" = true ] 
    then
	header=false
    else 
    	$DIR/do-single-test.sh $line $MEASUREMENTS_FILE
    fi
done < "$TEST_FILE"
