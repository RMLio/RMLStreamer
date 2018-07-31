#!/bin/bash
#
# RML Framework run script
#
# Created by Wouter Maroy
#
#



# Default options are as follows:
# MAPPINGPATH = src/main/resources/json_stream_data_mapping.ttl
# OUTPUTPATH =
# SOCKET = 9000

PROPERTY_FILE=configuration.properties

function getProperty {
   PROP_KEY=$1
   PROP_VALUE=`cat $PROPERTY_FILE | grep "$PROP_KEY" | cut -d'=' -f2`
   echo $PROP_VALUE
}

echo "# Reading property from $PROPERTY_FILE"
FLINKDIR=$(getProperty "flinkdir")
MAPPINGPATH=$(getProperty "mappingPath")
OUTPUTPATH=$(getProperty  "outputPath")
SOCKET=$(getProperty "socket")


echo "mapping: $MAPPINGPATH"
echo "output: $OUTPUTPATH"
echo "socekt: $SOCKET"

echo ""
echo "// RML Run Script"
echo "------------------------------------------"
echo ""

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
	*)
	POSITIONAL+=("$1")
	shift
	;;


esac
done
set -- "${POSITIONAL[@]}"


# Check if $MAPPINGPATH is set
if [ ! -z "$MAPPINGPATH"  ]; then
	# Execute
	bash $FLINKDIR run  -c io.rml.framework.Main target/framework-1.0-SNAPSHOT.jar --path $MAPPINGPATH --outputPath $OUTPUTPATH --socket $SOCKET
else
	echo "Execution aborted: -p|--path must be given."
	echo ""
	echo "-------------------------------------------"
	echo ""
fi