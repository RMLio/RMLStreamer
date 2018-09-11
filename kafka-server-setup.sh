#!/usr/bin/env bash


SCRIPT_USAGE_ERR_MSG="USAGE ./kafka-server-setup.sh -zp [zookeeper.properties] -bp [broker.properties] -cp  [connector_source.properties] [connector_sink.properties]"

function getProperty {
   PROP_KEY=$2
   PROP_VALUE=`cat $1 | grep "$PROP_KEY" | cut -d'=' -f2`
   echo $PROP_VALUE
}

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

        -cp|--connector-properties)
        #Connector script needs two property files for source and sink
            CONNECTOR_SOURCE_PROPERTIES="$2"
            CONNECTOR_SINK_PROPERTIES="$3"
        shift
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




ZOOKEEPER="${KAFKADIR}bin/zookeeper-server-start.sh"
BROKERSERVER="${KAFKADIR}bin/kafka-server-start.sh"
CONNECTOR="${KAFKADIR}bin/connect-standalone.sh"

SCRIPT_FILES=("$ZOOKEEPER" "$BROKERSERVER" "$CONNECTOR")
PROPERTY_FILES=("$ZOOKEEPER_PROPERTY" "$BROKER_PROPERTY" "$CONNECTOR_SOURCE_PROPERTIES" "$CONNECTOR_SINK_PROPERTIES")
PROPERTY_REF_FILES=("zookeeper" "broker" "connector source"  "connector sink")


#Check script files existence
for i in "${!SCRIPT_FILES[@]}"; do
    checkFileThrowMsg "${SCRIPT_FILES[$i]}" "The following required script doesn't exists:"
done

#Check property files existence
for i in "${!PROPERTY_FILES[@]}"; do
    checkFileThrowMsg "${PROPERTY_FILES[$i]}" "The required property file for ${PROPERTY_REF_FILES[$i]} doesn't exists:"
done


echo "----------------------------------------------"
