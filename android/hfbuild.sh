#!/bin/bash

# Command line build tool.
# Use like ant:
# -- hfbuild <sku> <debug or release> <build string> <password> [install to device]
# -- hfbuild release|amazon debug|release 201112060900 password install

EXPECTED_ARGS=4
E_BADARGS=65

usage()
{
    echo "hfbuild - command line tool to build HFGame indicating sku as first parameter"
    echo "usage: hfbuild <release|amazon> <debug|release> <buildstring> <password> [install]"
    echo ""
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
    mkdir build
    mkdir build/assets
    mkdir build/gen
    mkdir build/keystore
    mkdir build/libs
    mkdir build/proguard
    mkdir build/res
    mkdir build/src
}

copyTrunkTreeToBuildTree()
{
    echo "---"
    echo "Copying trunk tree to build tree"
    cp -R assets build
    cp -R keystore build
    cp -R libs build
    cp -R proguard build
    cp -R res build
    cp -R src build
    cp ./.classpath build
    cp ./.project build
    cp ./AndroidManifest.xml build
    cp ./project.properties build
    cp ./build.xml build
    cp ./ant.properties build
    cp ./local.properties build
}

setBuildStringForRelease()
{
    echo "---"
    echo "Setting build string for release ski"
    sed -i -e 's/g000000000000/'$1'/g' build/src/com/hyperfine/hfgame/Config.java
}

fixAntPropertiesForPassword()
{
    echo "---"
    echo "Fix build/ant.properties to hold password"
    sed -i -e 's/foo.store.password=foo/key.store.password='$1'/g' build/ant.properties
    sed -i -e 's/foo.alias.password=foo/key.alias.password='$1'/g' build/ant.properties
}

moveToBuildDirectory()
{
    echo "---"
    echo "Moving to build directory"
    cd build
}

invokeAnt()
{
    echo "---"
    echo "Invoking ant to build the project"
    ant clean
    ant $1
}

returnToMainDirectory()
{
    echo "---"
    echo "Returning to invoked directory"
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

echo "Ready to start..."
removeOldBuildTree
createBuildDirectory
copyTrunkTreeToBuildTree
fixAntPropertiesForPassword $4

# Here is where we'd branch on build type. For now, it's release only

setBuildStringForRelease $3
moveToBuildDirectory
invokeAnt $2
returnToMainDirectory

echo "Done!"

exit 0
