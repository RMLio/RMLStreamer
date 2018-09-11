#!/usr/bin/env bash

echo "----------------------------------------------------------------------------------"
echo "Checking if required kafka binaries exist in current dir."
echo "Script will download kafka binary versions which are absent in the current dir...."
echo "----------------------------------------------------------------------------------"

function findKafkaDir {
    NAME="$1"
    find . -type d -name "$NAME" -print
}

KAFKA08="$(findKafkaDir "kafka*0.8*" )"
KAFKA09="$(findKafkaDir "kafka*0.9*" )"
KAFKA010="$(findKafkaDir "kafka*0.10*" )"

KAFKA08SOURCE="https://archive.apache.org/dist/kafka/0.8.2.2/kafka_2.10-0.8.2.2.tgz"
KAFKA09SOURCE="https://archive.apache.org/dist/kafka/0.9.0.1/kafka_2.10-0.9.0.1.tgz"
KAFKA010SOURCE="http://apache.cu.be/kafka/0.10.2.2/kafka_2.10-0.10.2.2.tgz"


kafkaFiles=("$KAFKA08" "$KAFKA09" "$KAFKA010")
kafkaLinks=("$KAFKA08SOURCE" "$KAFKA09SOURCE" "$KAFKA010SOURCE")

for i in "${!kafkaFiles[@]}"; do
    if [ ! -d "${kafkaFiles[$i]}" ]; then
        echo "Downloading required files from ${kafkaLinks[$i]}...."
        echo "-----"
        wget -qO- "${kafkaLinks[$i]}" | tar -xz -C ./
    fi
done


echo "Done with verification of kafka binaries"

