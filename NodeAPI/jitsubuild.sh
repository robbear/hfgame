#!/bin/bash

# Command line build tool to build hfapi
# usage: jitsubuild <test1|test2|test3|staging|production>

EXPECTED_ARGS=1
E_BADARGS=65
PACKAGE_NAME=""
BUILD_TARGET=""

set -o pipefail     # trace ERR through pipes
set -o errtrace     # trace ERR through 'time command' and other functions

error_handler()
{
  JOB="$0"          # job name
  LASTLINE="$1"     # line of error occurrence
  LASTERR="$2"      # error code
  echo "ERROR in ${JOB} : line ${LASTLINE} with exit code ${LASTERR}"
  exit 1
}

trap 'error_handler ${LINENO} ${$?}' ERR

usage()
{
  echo "jitsubuild - command line tool to build hfapi"
  echo "usage: ./jitsubuild.sh <test1|test2|test3|staging|production>"
  echo ""
}

createVersionFile()
{
  echo "---"
  echo "Building version.txt file"
  git rev-parse HEAD > ./NodeWebSite/version.txt
}

removeOldBuildTree()
{
  echo "---"
  echo "Removing any existing build tree"
  rm -rf ./build
}

createBuildDirectory()
{
  echo "---"
  echo "Creating build directory"
  mkdir ./build/
  mkdir ./build/NodeWebSite/
  cp -R ./NodeWebSite/config/ ./build/NodeWebSite/config/
  cp -R ./NodeWebSite/logger/ ./build/NodeWebSite/logger/
  cp -R ./NodeWebSite/routes/ ./build/NodeWebSite/routes/
  cp ./NodeWebSite/*.js ./build/NodeWebSite/
  cp ./NodeWebSite/package.json ./build/NodeWebSite/
  cp ./NodeWebSite/version.txt ./build/NodeWebSite/
  mkdir ./build/NodeWebSite/logfiles/
  cp ./NodeWebSite/logfiles/readme.txt ./build/NodeWebSite/logfiles/
  cp -R ../models ./build/NodeWebSite/models/
  rm -rf ./build/NodeWebSite/models/node_modules/
}

runTimeStamper()
{
  echo "---"
  echo "Build timestamp.txt file"
  date +%Y%m%d%H%M%S > ./build/NodeWebSite/timestamp.txt
}

runStylesCacheBuster()
{
  echo "---"
  echo "Running sed to cache-bust image files in styles"
  timeStamp=$(cat ./build/NodeWebSite/timestamp.txt)

  DIRS="./build/NodeWebSite/public/styles/ ./build/NodeWebSite/public/rfi/styles/ ./build/NodeWebSite/public/ct/styles/"

  for d in $DIRS
  do
    for f in $d*.css
    do
      sed -e "s/.png/.png?$timeStamp/g" -e "s/.jpg/.jpg?$timeStamp/g" -e "s/.jpeg/.jpeg?timeStamp/g" -e "s/.gif/.gif?timeStamp/g" <$f >./build/temp.css
      rm $f
      mv ./build/temp.css $f
    done
  done
}

editConfigFile()
{
  echo "---"
  echo "Running sed to modify the config.js file for Cloud deployment"

  sed -e "s/..\/..\/..\/models\//..\/models\//g" -e "s/localhost:27017/hfmongo:D0ntBl1nk@ds053497.mongolab.com:53497/g" <./build/NodeWebSite/config/config.js >./build/temp.js
  rm ./build/NodeWebSite/config/config.js
  mv ./build/temp.js ./build/NodeWebSite/config/config.js
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
  'test1')
    PACKAGE_NAME="HFTest1"
    ;;
  'test2')
    PACKAGE_NAME="HFTest2"
    ;;
  'test3')
    PACKAGE_NAME="HFTest3"
    ;;
  'staging')
    PACKAGE_NAME="Staging"
    ;;
  'production')
    PACKAGE_NAME="Production"
    ;;
  *)
    usage
    exit $E_BADARGS
esac

BUILD_TARGET=$1

echo "PACKAGE_NAME is set to $PACKAGE_NAME"
echo "BUILD_TARGET is set to $BUILD_TARGET"

createVersionFile
removeOldBuildTree
createBuildDirectory
runTimeStamper
#runStylesCacheBuster
editConfigFile
echo "Done!"
exit 0