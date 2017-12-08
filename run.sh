#!/bin/bash
#
# RML Framework run script
#
# Created by Wouter Maroy
#
#

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
	bash ~/Downloads/flink-1.3.2/bin/flink run  -c io.rml.framework.Main target/framework-1.0-SNAPSHOT.jar --path $MAPPINGPATH
else
	echo "Execution aborted: -p|--path must be given."
	echo ""
	echo "-------------------------------------------"
	echo ""
fi

