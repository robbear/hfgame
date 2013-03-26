#!/bin/bash

# Command line test tool
# usage: runtests <all|models>

EXPECTED_ARGS=1
E_BADARGS=65

set -o pipefail
set -o errtrace

error_handler()
{
    JOB="$0"         # job name
    LASTLINE="$1"    # line of error occurrence
    LASTERR="$2"     # error code
    echo "ERROR in ${JOB} : line ${LASTLINE} with exit code ${LASTERR}"
    exit 1
}

trap 'error_handler ${LINENO} ${$?}' ERR

usage()
{
    echo "runtests - command line test tool"
    echo "usage: ./runtests.sh <all|models>"
    echo ""
}

runAll()
{
    runModelsTest
}

runModelsTest()
{
    ./node_modules/.bin/mocha --reporter spec ./models/*.js
}

#
# Check for expected arguments
#
if [ $# -lt $EXPECTED_ARGS ]
then
    usage
    exit $E_BADARGS
fi

case "$1" in
    'all')
	runAll
	;;
    'models')
	runModelsTest
	;;
    *)
	usage
	exit $E_BADARGS
esac

exit 0
