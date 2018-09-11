#!/usr/bin/env bash

SLEEP_MS=6

function spin {
    sp="/-\|"
    j=1
    i=0
    echo -n " "
    while (($i<=SLEEP_MS)); do
             i=$i+1
             printf "\b${sp:j++%${#sp}:1}"
             sleep 1
    done
    echo ""
}

file="/tmp/kafka-test-pids"
if [ -f "$file" ]; then
   echo "$file found."
   echo "Stopping all found kafka processes noted in the file."

   while IFS='=' read -r key value
   do
        nKey=$(echo $key | tr '.' '_')
        eval ${nKey}=${value}
   done < "$file"

   echo "Killing connector processes... (pid: ${connector_pid})"
   spin $SLEEP_MS
   kill -9 ${connector_pid}
   echo "Killed"


   echo "Killing broker processes... (pid: ${broker_pid})"
   spin $SLEEP_MS
   kill -9 ${broker_pid}
   echo "Killed"

   echo "Killing zookeeper processes... (pid: ${zookeeper_pid})"
   spin $SLEEP_MS
   kill -9 ${zookeeper_pid}
   echo "Killed"
fi

