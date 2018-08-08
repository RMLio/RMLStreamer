#!/bin/bash

# Fetch arguments
POSITIONAL=()

while [[ $# -gt 0 ]] 
do
    key="$1"
    case $key in 
        -c|--clean)
            CLEAN=true
        shift
        ;;
        -t|--type)
            STREAMTYPE="$2"
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

if [ -z "$STREAMTYPE" ]; then
    echo "" 
    echo "You must give the input stream type of the test with -t|--type [kafka,tcp,file]"
    echo "-------------------------------------------------------------------------------"
    echo ""
    exit 1
fi



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



find src/test/resources/stream/$STREAMTYPE -type d -name "RMLTC*" |
    sort | 
    grep "stream/.*RMLTC.*" -o |
    sed 's/\(.*\)/--path \1/'| 
    tr "\n" "\0" |
    xargs -0 -i -n1 mvn exec:java -Dexec.mainClass="io.rml.framework.StreamingTestMain"  -Dexec.classpathScope="test" -Dexec.args="{} --type $STREAMTYPE" |
    tr "\n" "\0" |
    xargs -0 -I% -n1 bash -c 'echo  "%" | egrep "^\[.*\][^\[\]]*|^<.*>|^_:" ;  echo "%" >&3'



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




