#!/bin/bash

# Command line build tool to build hfgame.com
# usage: awsbuild.sh <test1|test2|test3|production>

EXPECTED_ARGS=1
E_BADARGS=65
BUILD_DIR="./build.aws"
SUBDOMAIN_NAME=""
BUILD_TARGET=""
DOMAINS_KEY=""

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
  echo "awsbuild - command line tool to build hfgame.com"
  echo "usage: ./awsbuild.sh <test1|test2|test3|production>"
  echo ""
}

createVersionFile()
{
  echo "---"
  echo "Building version.txt file"
  git rev-parse HEAD > ./NodeWebSite/version.txt
}

createBuildDirectory()
{
  echo "---"
  echo "Creating build directory"
  if [ ! -d $BUILD_DIR ]; then
      mkdir $BUILD_DIR/
  fi
  if [ ! -d $BUILD_DIR/NodeWebSite ]; then
      mkdir $BUILD_DIR/NodeWebSite/
  fi
  if [ ! -d $BUILD_DIR/NodeWebSite/.ebextensions ]; then
      mkdir $BUILD_DIR/NodeWebSite/.ebextensions/
  fi
  if [ ! -d $BUILD_DIR/NodeWebSite/logfiles ]; then
      mkdir $BUILD_DIR/NodeWebSite/logfiles/
  fi
  cp -R ./NodeWebSite/config/ $BUILD_DIR/NodeWebSite/config/
  cp -R ./NodeWebSite/languages/ $BUILD_DIR/NodeWebSite/languages/
  cp -R ./NodeWebSite/logger/ $BUILD_DIR/NodeWebSite/logger/
  cp -R ./NodeWebSite/public/ $BUILD_DIR/NodeWebSite/public/
  cp -R ./NodeWebSite/routes/ $BUILD_DIR/NodeWebSite/routes/
  cp -R ./NodeWebSite/views/ $BUILD_DIR/NodeWebSite/views/
  cp ./NodeWebSite/*.js $BUILD_DIR/NodeWebSite/
  cp ./NodeWebSite/package.json $BUILD_DIR/NodeWebSite/
  cp ./NodeWebSite/version.txt $BUILD_DIR/NodeWebSite/
  cp ./NodeWebSite/logfiles/readme.txt $BUILD_DIR/NodeWebSite/logfiles/
  cp ./aws-deployments/$BUILD_TARGET/hfgamedotcom.config $BUILD_DIR/NodeWebSite/.ebextensions/
  cp ./aws-deployments/$BUILD_TARGET/package.json $BUILD_DIR/NodeWebSite/
}

runTimeStamper()
{
  echo "---"
  echo "Build timestamp.txt file"
  date +%Y%m%d%H%M%S > $BUILD_DIR/NodeWebSite/timestamp.txt
}

runStylesCacheBuster()
{
  echo "---"
  echo "Running sed to cache-bust image files in styles"
  timeStamp=$(cat $BUILD_DIR/NodeWebSite/timestamp.txt)

  DIRS="$BUILD_DIR/NodeWebSite/public/styles/

  for d in $DIRS
  do
    for f in $d*.css
    do
      sed -e "s/.png/.png?$timeStamp/g" -e "s/.jpg/.jpg?$timeStamp/g" -e "s/.jpeg/.jpeg?timeStamp/g" -e "s/.gif/.gif?timeStamp/g" <$f >$BUILD_DIR/temp.css
      rm $f
      mv $BUILD_DIR/temp.css $f
    done
  done
}

fixPackageJsonForDeployment()
{
  echo "---"
  echo "Updating package.json with deployment subdomain"
  sed -e "s/\"name\": \"hfgamedotcom\"/\"name\": \"$SUBDOMAIN_NAME\"/g" -e "s/\"subdomain\": \"hfgamedotcom\"/\"subdomain\": \"$SUBDOMAIN_NAME\"/g" -e "s/\"domains\"/\"$DOMAINS_KEY\"/g" <$BUILD_DIR/NodeWebSite/package.json >$BUILD_DIR/temp.json
  rm $BUILD_DIR/NodeWebSite/package.json
  mv $BUILD_DIR/temp.json $BUILD_DIR/NodeWebSite/package.json
}

zipProject()
{
  echo "---"
  echo "Zipping up project for upload to AWS"
  cd $BUILD_DIR
  zip -r -q ./NodeWebSite.zip ./NodeWebSite/*
  cd ..
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
    SUBDOMAIN_NAME="hftest1"
    DOMAINS_KEY="domains_ignore"
    ;;
  'test2')
    SUBDOMAIN_NAME="hftest2"
    DOMAINS_KEY="domains_ignore"
    ;;
  'test3')
    SUBDOMAIN_NAME="hftest3"
    DOMAINS_KEY="domains_ignore"
    ;;
  'production')
    SUBDOMAIN_NAME="hfgamedotcom"
    DOMAINS_KEY="domains"
    ;;
  *)
    usage
    exit $E_BADARGS
esac

BUILD_TARGET=$1

echo "SUBDOMAIN_NAME is set to $SUBDOMAIN_NAME"
echo "BUILD_TARGET is set to $BUILD_TARGET"

createVersionFile
createBuildDirectory
runTimeStamper
runStylesCacheBuster
fixPackageJsonForDeployment
zipProject
echo "Done!"
exit 0
