#!/bin/bash

# Fetch arguments
POSITIONAL=()
TESTCLASS="io.rml.framework.StreamingTestMain"

while [[ $# -gt 0 ]] 
do
    key="$1"
    case $key in 
        -c|--clean)
            CLEAN=true
        shift
        shift
        ;;
        -t|--test)
            TESTCLASS="$2"
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

echo ""
echo "// STREAM TEST SCRIPT"
echo "-----------------------------------------"
echo "" 

if [ ! -z "$CLEAN" ]; then
    echo ""
    echo "Recompiling test classes......"
    echo "-------------------------------------"
    echo ""
    mvn test -DskipTests
    echo ""
    echo "------------------------------"
    echo "Waiting 5 seconds..." 
    echo "------------------------------"
    echo ""
    sleep 5
fi

#Create temp file for logging output and grepping failed test cases
temp_test_log=$(mktemp)
exec 3>"$temp_test_log"
exec 4<"$temp_test_log"

rm "$temp_test_log"



find src/test/resources/stream -type d -name "RMLTC*" |
    sort | 
    grep "stream/.*RMLTC.*" -o |
    sed 's/\(.*\)/--path \1/'| 
    tr "\n" "\0" |
    xargs -0 -i -n1 mvn exec:java -Dexec.mainClass="$TESTCLASS"  -Dexec.classpathScope="test" -Dexec.args={} |
    xargs -I%  bash -c 'echo "%" | egrep "^\[.*\][^\[\]]*|^<.*>|^_:" ;  echo "%" >&3'



echo "" 
echo "" 
echo "----------------------------------------------------"
echo "Failed test cases: "
echo "----------------------------------------------------"
echo "" 
echo "" 

while read error_line 
do
  
   candid=$(echo "$error_line"| grep --color=always "^\[ERROR\].*")
   
   if [ ! -z "$candid" ]; then 
       echo $candid 
   fi

   line_break=$(echo "$error_line" | grep -i "Test case:")
   if [ ! -z "$line_break" ]; then
       echo "" 
       echo "============================================================="
       echo ""
   fi    

done <&4 




