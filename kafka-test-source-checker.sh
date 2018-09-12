#!/usr/bin/env bash

echo "----------------------------------------------------------------------------------"
echo "Checking if required kafka binaries exist in current dir."
echo "Script will download kafka binary versions which are absent in the current dir...."
echo "----------------------------------------------------------------------------------"

function findKafkaDir {
    NAME="$1"
    find . -type d -name "$NAME" -print
}


PROPERTY_FILE="kafka_test.properties"

function getProperty {
   PROP_KEY=$1
   PROP_VALUE=`cat $PROPERTY_FILE | grep "$PROP_KEY" | cut -d'=' -f2`
   echo $PROP_VALUE
}

KAFKA08="$(findKafkaDir "kafka*0.8*" )"
KAFKA09="$(findKafkaDir "kafka*0.9*" )"
KAFKA010="$(findKafkaDir "kafka*0.10*" )"

KAFKA08SOURCE=$(getProperty "kafka08.download.link")
KAFKA09SOURCE=$(getProperty "kafka09.download.link")
KAFKA010SOURCE=$(getProperty "kafka010.download.link")


kafkaFiles=("$KAFKA08" "$KAFKA09" "$KAFKA010")
kafkaLinks=($KAFKA08SOURCE $KAFKA09SOURCE $KAFKA010SOURCE)

for i in "${!kafkaFiles[@]}"; do
    if [ ! -d "${kafkaFiles[$i]}" ]; then
        echo "Downloading required files from ${kafkaLinks[$i]}...."
        echo "-----"
        sanitizedLink="${kafkaLinks[$i]%\"}"
        sanitizedLink="${sanitizedLink#\"}"
        wget -O- $sanitizedLink | tar -xz -C ./
    fi
done


echo "Done with verification of kafka binaries"

