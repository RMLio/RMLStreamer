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
    mvn clean package -DskipTests
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

function checkFile {
    FILE="$1"
    MSG="$2"

    if [ -z "$FILE" ]; then

        echo "$2"
        exit 1
    fi

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
    STREAMER_JAR=$(ls target/RMLStreamer*)
    echo "Streamer jar: $STREAMER_JAR"

    VERSIONPATTERN="$1"
    rm "test.sink.txt" > /dev/null
    
    FLINKBIN="$(getProperty "flinkBin")"
    
    MAPPING="$(getProperty "mapping")"
    INPUT="$(getProperty "data-input")"
    
    checkFile "$MAPPING" "[ERROR] RML mapping file doesn't exists: ${MAPPING}" 
    checkFile "$INPUT" "[ERROR] Input data file doesn't exists: ${INPUT}"

    KAFKADIR="$(findKafkaDir ${VERSIONPATTERN})"
    ZOOKEEPER_PROPERTY="${KAFKADIR}/config/zookeeper.properties"
    BROKER_PROPERTY="${KAFKADIR}/config/server.properties"

    PRODUCER="${KAFKADIR}/bin/kafka-console-producer.sh"
    CONSUMER="${KAFKADIR}/bin/kafka-console-consumer.sh"
    TOPIC="${KAFKADIR}/bin/kafka-topics.sh"

    
    zookeeper_connection="$(getProperty "zookeeper.connection")"
    broker_list="$(getProperty "broker-list")"
    topic="$(getProperty "rdf-source-topic")"
    rdf_test_topic="$(getProperty "rdf-test-topic")"

    runCommand "./kafka-test-server-setup.sh -d "${KAFKADIR}/" -zp "${ZOOKEEPER_PROPERTY}" -bp "${BROKER_PROPERTY}"" "Starting server from ${KAFKADIR}"
    if [ $? -eq 0 ]; then
            echo "[INFO] Running rml streamer with options --broker-list ${broker_list} --topic $rdf_test_topic" 
            sleep 5
            bash ${TOPIC} --create --zookeeper ${zookeeper_connection} --replication-factor 1 --partitions 1 --topic ${topic}
            bash ${TOPIC} --create --zookeeper ${zookeeper_connection} --replication-factor 1 --partitions 1 --topic ${rdf_test_topic}            
            echo "---------------------------"
            echo "Topic created!!"
            echo "---------------------------"

            
           bash $FLINKBIN  run -c io.rml.framework.Main $STREAMER_JAR -path "$MAPPING" --broker-list "${broker_list}" --topic "$rdf_test_topic" &            
          
           FLINK_PID=$!            

            read -p "[INFO] Press enter to produce input for flink with kafka producer" 
            bash ${PRODUCER} --broker-list ${broker_list} --topic ${topic} < "${INPUT}"
            echo "--------------------------------------"
            echo "Producer has written to a kafka broker"
            echo "--------------------------------------"
 
            sleep 5 
            read -p "[INFO] Press enter to collect generated rdf triple in test.sink.txt" 
            bash ${CONSUMER} --zookeeper ${zookeeper_connection} --topic ${rdf_test_topic} --from-beginning > "test.sink.txt"  &
            consumer_pid=$!
            echo "------------------------------------------------"
            echo "Consumer has written rml output to test.sink.txt"
            echo "------------------------------------------------"
            sleep 5
            kill -9 ${consumer_pid}

            kill -SIGINT ${FLINK_PID}
    fi
    read -p "Press enter to continue the test for the next supported kafka version"

}


./kafka-test-source-checker.sh

echo "" 
echo "---------------------------------------------"
PS3="Choose the kafka version server for testing..."
options=("Kafka08" "Kafka09" "Kafka010")

select opt in "${options[@]}"
do

    case $opt in 
        "Kafka08")
            testServerVersion "kafka*0.8*" "0.8.x"

            break
            ;;


        "Kafka09")
            testServerVersion "kafka*0.9*" "0.9.x"

            break
            ;;

        "Kafka010")
            testServerVersion "kafka*0.10*" "0.10.x"
            break
            ;;


        *)
            echo "invalid option" ;;



    esac


done


./kafka-test-stop-all.sh

