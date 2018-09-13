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


   echo "Killing broker processes... (pid: ${broker_pid})"
   spin $SLEEP_MS
   kill -9 ${broker_pid}
   echo "Killed"

   echo "Killing zookeeper processes... (pid: ${zookeeper_pid})"
   spin $SLEEP_MS
   kill -9 ${zookeeper_pid}
   echo "Killed"

   rm "${file}"
fi



echo "[WARNING]: The script will now attempt to clear the /tmp dir of kafka/zookeeper logs" 
read -p "Press enter to confirm deletion of log files for kafka in /tmp"


rm -rf /tmp/kafka-logs* /tmp/zookeeper*

