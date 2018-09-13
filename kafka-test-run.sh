#!/usr/bin/env bash


POSITIONAL=()

while [[ $# -gt 0 ]]
do
    key="$1"
    case $key in
        -c|--clean)
            CLEAN=true
        shift
        ;;

        -v|--verbose)
            VERBOSE=true
        shift
        ;;
        *)
           POSITIONAL+=("$1")
        shift
        ;;
    esac
done

set -- "${POSITIONAL[@]}"

echo ""
echo "// KAFKA TEST SCRIPT"
echo "-----------------------------------------"
echo ""


if [ ! -z "$CLEAN" ]; then
    echo ""
    echo "Recompiling test classes......"
    echo "-------------------------------------"
    echo ""
    mvn test -DskipTests
    echo ""
    echo "------------------------------"
    echo "Waiting 5 seconds..."
    echo "------------------------------"
    echo ""
    sleep 5
fi

PROPERTY_FILE="kafka_test.properties"

function getProperty {
   PROP_KEY=$1
   PROP_VALUE=`cat $PROPERTY_FILE | grep "$PROP_KEY" | cut -d'=' -f2`
   echo $PROP_VALUE
}


function findKafkaDir {
    NAME="$1"
    find . -type d -name "$NAME" -print
}


function runCommand {
    echo "$2"
    if [ ! -z "$VERBOSE" ]; then
       echo "VERBOSE!"
       $1
    else
        echo "NOT VERBOSE!"
       $1  &>/dev/null
    fi
}

function testServerVersion {
    VERSIONPATTERN="$1"
    rm "test.sink.txt" > /dev/null
    KAFKADIR="$(findKafkaDir ${VERSIONPATTERN})"
    ZOOKEEPER_PROPERTY="${KAFKADIR}/config/zookeeper.properties"
    BROKER_PROPERTY="${KAFKADIR}/config/server.properties"

    PRODUCER="${KAFKADIR}/bin/kafka-console-producer.sh"
    CONSUMER="${KAFKADIR}/bin/kafka-console-consumer.sh"
    TOPIC="${KAFKADIR}/bin/kafka-topics.sh"


    zookeeper_connection="$(getProperty "zookeeper.connection")"
    broker_list="$(getProperty "broker-list")"
    topic="$(getProperty "topic")"

    runCommand "./kafka-test-server-setup.sh -d "${KAFKADIR}/" -zp "${ZOOKEEPER_PROPERTY}" -bp "${BROKER_PROPERTY}"" "Starting server from ${KAFKADIR}"
    if [ $? -eq 0 ]; then
        if [ ! -f "test.txt" ]; then
            echo "Test data source file for kafka producer doesn't exists: test.txt"
            
        else

            sleep 5
            bash ${TOPIC} --create --zookeeper ${zookeeper_connection} --replication-factor 1 --partitions 1 --topic ${topic}
            echo "---------------------------"
            echo "Topic created!!"
            echo "---------------------------"

            bash ${PRODUCER} --broker-list ${broker_list} --topic ${topic} < "test.txt"
            echo "--------------------------------------"
            echo "Producer has written to a kafka broker"
            echo "--------------------------------------"

            bash ${CONSUMER} --zookeeper ${zookeeper_connection} --topic ${topic} --from-beginning > "test.sink.txt"  &
            consumer_pid=$!
            echo "------------------------------------------------"
            echo "Consumer has written rml output to test.sink.txt"
            echo "------------------------------------------------"
            sleep 5
            kill -9 ${consumer_pid}
        fi
    fi
    read -p "Press enter to continue the test for the next supported kafka version"

}


./kafka-test-source-checker.sh

testServerVersion "kafka*0.8*"
./kafka-test-stop-all.sh

testServerVersion "kafka*0.9*"
./kafka-test-stop-all.sh

testServerVersion "kafka*0.10*"
./kafka-test-stop-all.sh
