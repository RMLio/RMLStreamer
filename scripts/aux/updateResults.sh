#!/bin/bash

POSITIONAL=()

while [[ $# -gt 0 ]]
do
    key="$1"
    case $key in
        -p|--passing)
            PASSING="$2"
        shift
        shift
        ;;

        -t|--type)
        TYPE="$2"
        shift
        shift
        ;;

        --st|--stream-type)
            STREAM_TYPE="$2"
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

if [ -z "$TYPE" ]; then 
    TYPE="static"
fi

declare -A PASSING_FOLDER 
PASSING_FOLDER=( ["static"]="rml-testcases" ["stream"]="stream/${STREAM_TYPE}")

cd ../../

TEST_RESOURCES="src/test/resources/"
NEGATIVE_TEST_CASES="negative_test_cases"
REPORT="${TEST_RESOURCES}report/${TYPE}/result.csv"
echo "${PASSING_FOLDER[@]}"
echo "Resetting test cases in report file $REPORT"
function resetTestCases {
    sed -ir 's/(.*JSON).*/\1,inapplicable/' $REPORT
    sed -ir 's/(.*CSV).*/\1,inapplicable/' $REPORT
    sed -ir 's/(.*XML).*/\1,inapplicable/' $REPORT
}


resetTestCases > /dev/null
NEGATIVE_TESTS="${TEST_RESOURCES}${NEGATIVE_TEST_CASES}"
POSITIVE_TESTS="${TEST_RESOURCES}${PASSING_FOLDER[${TYPE}]}"
echo "==============="
echo "Positive test cases parent folder: "
echo "$POSITIVE_TESTS"
echo "Negative test cases parent folder: "
echo "$NEGATIVE_TESTS"
echo "==============="


# Set passing test cases 

function setPassingTestCases {
     SEARCH_FOLDER="$1"
find $SEARCH_FOLDER -type d -name "RMLTC*" | xargs -I{} basename {} | xargs -I{} sed -ir 's/{},.*/{},passing/' $REPORT 

}

setPassingTestCases  "$POSITIVE_TESTS" 
setPassingTestCases  "$NEGATIVE_TESTS"

