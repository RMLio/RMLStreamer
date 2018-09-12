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

function findKafkaDir {
    NAME="$1"
    find . -type d -name "$NAME" -print
}

function testServerVersion {
    VERSIONPATTERN="$1"

    KAFKADIR="$(findKafkaDir ${VERSIONPATTERN})"
    echo "$KAFKADIR"
    ZOOKEEPER_PROPERTY="${KAFKADIR}/config/zookeeper.properties"
    BROKER_PROPERTY="${KAFKADIR}/config/server.properties"

    ./kafka-test-server-setup.sh -d "${KAFKADIR}/" -zp "${ZOOKEEPER_PROPERTY}" -bp "${BROKER_PROPERTY}"
}


./kafka-test-source-checker.sh

testServerVersion "kafka*0.8*"
./kafka-test-stop-all.sh

testServerVersion "kafka*0.9*"
./kafka-test-stop-all.sh

testServerVersion "kafka*0.10*"
./kafka-test-stop-all.sh
