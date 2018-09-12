#!/usr/bin/env bash

SLEEP_TIME=15
TMP_KAFKA_PID="/tmp/kafka-test-pids"
SCRIPT_USAGE_ERR_MSG="USAGE ./kafka-test-server-setup.sh [-d kafka directory] -zp [zookeeper.properties] -bp [broker.properties] "


function checkFileThrowMsg {
    MSG=$2
    FILE=$1
    if [ ! -f "$FILE" ]; then
        echo "$SCRIPT_USAGE_ERR_MSG"
        echo "-----------------------------------------------------------------------------"
        echo "$MSG"
        echo "[ERROR] File name: $FILE"
        exit 1
    fi
}


echo ""
echo "------------------------------------------------------------------------------------"
echo "Setting up kafka server..."
echo "------------------------------------------------------------------------------------"

# Fetch arguments
POSITIONAL=()

while [[ $# -gt 0 ]]
do
    key="$1"
    case $key in

        -d|--kafka-dir)
            KAFKADIR="$2"
        shift
        shift
        ;;
        -zp|--zookeeper-property)
            ZOOKEEPER_PROPERTY="$2"
        shift
        shift
        ;;

        -bp|--broker-proerpty)
            BROKER_PROPERTY="$2"
        shift
        shift
        ;;

        *)
           POSITIONAL+=("$1")
        shift
        ;;
    esac
done

set -- "${POSITIONAL[@]}"



KAFKADIR="${KAFKADIR}bin/"

ZOOKEEPER="${KAFKADIR}zookeeper-server-start.sh"
BROKERSERVER="${KAFKADIR}kafka-server-start.sh"


SCRIPT_FILES=("$ZOOKEEPER" "$BROKERSERVER" )
PROPERTY_FILES=("$ZOOKEEPER_PROPERTY" "$BROKER_PROPERTY" )
PROPERTY_REF_FILES=("zookeeper" "broker" )


#Check script files existence
for i in "${!SCRIPT_FILES[@]}"; do
    checkFileThrowMsg "${SCRIPT_FILES[$i]}" "The following required script doesn't exists:"
done

#Check property files existence
for i in "${!PROPERTY_FILES[@]}"; do
    checkFileThrowMsg "${PROPERTY_FILES[$i]}" "The required property file for ${PROPERTY_REF_FILES[$i]} doesn't exists:"
done



echo ""
echo "-------------------------------------------------------------"
echo "Starting up kafka server with required files from ${KAFKADIR}"
echo "-------------------------------------------------------------"
echo ""

echo
echo "----------------------------------------------"
echo "Starting zookeeper server...."

bash ${ZOOKEEPER} "${ZOOKEEPER_PROPERTY}" &
ZOOKEEPER_PID=$!

sleep $SLEEP_TIME
echo
echo "Starting broker server...."
echo "-----------------------------------------------"
bash ${BROKERSERVER} "${BROKER_PROPERTY}" &
BROKER_PID=$!



echo "zookeeper.pid=${ZOOKEEPER_PID}" > ${TMP_KAFKA_PID}
echo "broker.pid=${BROKER_PID}" >> ${TMP_KAFKA_PID}
