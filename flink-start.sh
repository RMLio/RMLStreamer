#!/usr/bin/env bash
PROPERTY_FILE="configuration.properties"

function getProperty {
   PROP_KEY=$1
   PROP_VALUE=`cat $PROPERTY_FILE | grep "$PROP_KEY" | cut -d'=' -f2`
   echo $PROP_VALUE
}


FLINKDIR="$(getProperty "flinkDir")"

FLINKLOCAL="${FLINKDIR}/bin/start-local.sh"
