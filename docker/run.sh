#!/bin/bash
#
# RML Framework run script
#
# Created by Wouter Maroy

function getPropertyFromConfig {
   PROP_KEY=$1
   PROP_VALUE=`cat $PROPERTY_FILE | grep "$PROP_KEY" | cut -d'=' -f2`
   echo $PROP_VALUE
}

function setProperties {
    echo $@
    # Fetch arguments
    POSITIONAL=()
    while [[ $# -gt 0 ]]
    do
    key="$1"

    case $key in
        -p|--path)
            MAPPINGPATH="$2"
            shift
            shift
            ;;
        -o|--outputPath)
            OUTPUTPATH="$2"
            shift # past argument
            shift # past value
            ;;
        -s|--socket)
            SOCKET="$2"
            shift # past argument
            shift # past value
            ;;
        -f|--flinkBin)
            FLINKBIN="$2"
            shift # past argument
            shift # past value
            ;;
        -b|--kafkaBrokerList)
            KAFKA_BROKERLIST="$2"
            shift # past argument
            shift # past value
            ;;
        -t|--kafkaTopic)
            KAFKA_TOPIC="$2"
            shift # past argument
            shift # past value
            ;;
        -c|--config)
            CONFIG="$2"
            shift # past argument
            shift # past value
            ;;
        *)
            POSITIONAL+=("$1")
            shift
            ;;
    esac
    done
    set -- "${POSITIONAL[@]}"

    #echo "mapping: $MAPPINGPATH"
    #echo "output: $OUTPUTPATH"
    #echo "socket: $SOCKET"
    #echo "config: $CONFIG"

    if [ ! -z "$CONFIG" ]; then
        PROPERTY_FILE=$CONFIG;
        echo "# Reading property from $PROPERTY_FILE"

        if [ -z "$MAPPINGPATH" ]; then
          MAPPINGPATH=$(getPropertyFromConfig "mappingPath")
        fi

        if [ -z "$OUTPUTPATH"  ]; then
          OUTPUTPATH=$(getPropertyFromConfig "outputPath")
        fi

        if [ -z "$SOCKET"  ]; then
          SOCKET=$(getPropertyFromConfig "socket")
        fi

        if [ -z "$FLINKBIN"  ]; then
          FLINKBIN=$(getPropertyFromConfig "flinkBin")
        fi

        if [ -z "$KAFKA_BROKERLIST"  ]; then
          KAFKA_BROKERLIST=$(getPropertyFromConfig "kafkaBrokerList")
        fi

        if [ -z "$KAFKA_TOPIC"  ]; then
          KAFKA_TOPIC=$(getPropertyFromConfig "kafkaTopic")
        fi
    fi
}

# we pass the script arguments to the function via $@
setProperties $@

echo "mapping: $MAPPINGPATH"
echo "output: $OUTPUTPATH"
echo "socket: $SOCKET"
echo "kafkaBrokerList: $KAFKA_BROKERLIST"
echo "kafkaTopic: $KAFKA_TOPIC"

echo ""
echo "// RML Run Script"
echo "------------------------------------------"
echo ""

# Check if $MAPPINGPATH is set
if [ ! -z "$MAPPINGPATH"  ]; then
	# Execute
	bash $FLINKBIN run  -c io.rml.framework.Main streamer.jar --path $MAPPINGPATH --outputPath $OUTPUTPATH --socket $SOCKET --broker-list $KAFKA_BROKERLIST --topic $KAFKA_TOPIC
else
	echo "Execution aborted: -p|--path must be given."
	echo ""
	echo "-------------------------------------------"
	echo ""
fi
