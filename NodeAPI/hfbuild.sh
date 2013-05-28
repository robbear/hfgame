#!/bin/bash

# Command line build tool to build hfapi
# usage: hfbuild <test1|test2|test3|production>

EXPECTED_ARGS=1
E_BADARGS=65
PACKAGE_NAME=""
BUILD_TARGET=""
BRANCH_NAME=""
CURRENT_LOCAL_BRANCH=""

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
  echo "hfbuild - command line tool to build hfapi"
  echo "usage: ./hfbuild.sh <test1|test2|test3|production>"
  echo ""
}

getCurrentLocalBranch()
{
  echo "---"
  echo "Noting current local branch"
  CURRENT_LOCAL_BRANCH=$(git branch | sed -n -e 's/^\* \(.*\)/\1/p')
  echo "CURRENT_LOCAL_BRANCH = $CURRENT_LOCAL_BRANCH"
}  

deleteRemoteBranch()
{
  echo "---"
  echo "Removing any remote branch named $BRANCH_NAME"
  trap '' ERR
  branchExists=$(git branch -r | grep $BRANCH_NAME)
  trap - ERR
  if [ -z "$branchExists" ]
  then
    echo "No remote branch exists with the name $BRANCH_NAME"
  else
    echo "A remote branch called $BRANCH_NAME exists. Removing it."
    git push origin --delete $BRANCH_NAME
  fi
}

createLocalBranch()
{
  echo "---"
  echo "Creating local branch $BRANCH_NAME via checkout"
  git checkout -b $BRANCH_NAME
}

pushRemoteBranch()
{
  echo "---"
  echo "Pushing $BRANCH_NAME to origin/$BRANCH_NAME"
  git push origin $BRANCH_NAME
}

deleteLocalBranch()
{
  echo "---"
  echo "Switching to $CURRENT_LOCAL_BRANCH and deleting local branch $BRANCH_NAME"
  git checkout $CURRENT_LOCAL_BRANCH
  git branch -d $BRANCH_NAME
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
    BRANCH_NAME="deploy-to-hftest1"
    ;;
  'test2')
    PACKAGE_NAME="HFTest2"
    BRANCH_NAME="deploy-to-hftest2"
    ;;
  'test3')
    PACKAGE_NAME="HFTest3"
    BRANCH_NAME="deploy-to-hftest3"
    ;;
  'production')
    PACKAGE_NAME="Production"
    BRANCH_NAME="deploy-to-production"
    ;;
  *)
    usage
    exit $E_BADARGS
esac

BUILD_TARGET=$1

echo "PACKAGE_NAME is set to $PACKAGE_NAME"
echo "BUILD_TARGET is set to $BUILD_TARGET"
echo "BRANCH_NAME is set to $BRANCH_NAME"

getCurrentLocalBranch
deleteRemoteBranch
createLocalBranch
pushRemoteBranch
deleteLocalBranch

echo "---"
echo "Now use the Azure Portal to upload deployments/$BUILD_TARGET/$PACKAGE_NAME.cspkg"
