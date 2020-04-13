#!/bin/bash
#
# RML Framework run script
#
# Created by Wouter Maroy

function getPropertyFromConfig {
   PROP_KEY=$1
   PROP_VALUE=$(cat $PROPERTY_FILE | grep "$PROP_KEY" | cut -d'=' -f2)
   echo $PROP_VALUE
}

function proertyInConfig() {
   PROP_KEY=$1
   IN_CONFIG=$(grep '$PROP_KEY' $PROPERTY_FILE)
   if [ -n "$IN_CONFIG" ]; then
     return true
    else
      return false
    fi
}

function setProperties {

    # Some default properties
    PARALLELISM=1

    echo $@
    # Fetch arguments
    POSITIONAL=()
    while [[ $# -gt 0 ]]
    do
    key="$1"

    case $key in
        -n|--job-name)
            JOBNAME="$2"
            shift # past argument
            shift # past value
            ;;
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

        --pp|--post-process)
            POSTPROCESS="$2"
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
        --pt|--partition-type)
            PARTITIONTYPE="$2"
            shift
            shift
            ;;
        --pi|--partition-id)
            PARTITIONID="$2"
            shift
            shift
            ;;
        -a|--parallelism)
            PARALLELISM="$2"
            shift # past argument
            shift # past value
            ;;
         -l|--enable-local-parallel)
            LOCAL_PARALLEL=true
            shift # option only
            ;;
         -bi|--base-iri)
            BASE_IRI="$2"
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

    if [ ! -z "$CONFIG" ]; then
        PROPERTY_FILE=$CONFIG;
        echo "# Reading property from $PROPERTY_FILE"

        if [ -z "$JOBNAME" ]; then
          JOBNAME=$(getPropertyFromConfig "jobName")
        fi

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

        if [ -z "$PARALLELISM"  ]; then
          PARALLELISM=$(getPropertyFromConfig "parallelism")
        fi

        if [ -z "$LOCAL_PARALLEL"  ]; then
          LOCAL_PARALLEL=$(propertyInConfig "enableLocalParallel")
        fi

        if [ -z "$BASE_IRI"  ]; then
          BASE_IRI=$(propertyInConfig "baseIRI")
        fi
    fi
}

# we pass the script arguments to the function via $@
setProperties $@

# find a jar...
STREAMER_JAR=$(ls target/RMLStreamer*)

echo "streamer jar: $STREAMER_JAR"
echo "job name: ${JOBNAME}"
echo "mapping: $MAPPINGPATH"
echo "output: $OUTPUTPATH"
echo "socket: $SOCKET"
echo "kafkaBrokerList: $KAFKA_BROKERLIST"
echo "kafkaTopic: $KAFKA_TOPIC"
echo "parallelism: $PARALLELISM"
echo "local parallelism enabled: $LOCAL_PARALLEL"
echo "base IRI: $BASE_IRI"

echo ""
echo "// RML Run Script"
echo "------------------------------------------"
echo ""

COMMANDLINE="$FLINKBIN run -p $PARALLELISM -c io.rml.framework.Main $STREAMER_JAR --job-name \"$JOBNAME\" --partition-id $PARTITIONID --partition-type $PARTITIONTYPE --post-process $POSTPROCESS --path $MAPPINGPATH --outputPath $OUTPUTPATH --socket $SOCKET --broker-list $KAFKA_BROKERLIST --topic $KAFKA_TOPIC --base-IRI $BASE_IRI"
if [ -n $LOCAL_PARALLEL]; then
  COMMANDLINE+=" --enable-local-parallel"
fi

# Check if $MAPPINGPATH is set
if [ ! -z "$MAPPINGPATH"  ]; then
	# Execute
	bash $COMMANDLINE
else
	echo "Execution aborted: -p|--path must be given."
	echo ""
	echo "-------------------------------------------"
	echo ""
fi
