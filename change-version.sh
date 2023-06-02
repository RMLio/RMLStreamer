#!/usr/bin/env bash

VERSION=$1

# exit if no version argument given
if [ ! "$VERSION" ]
then
	echo 'No version given. Exiting'.
	exit
fi

echo 'Updating pom.xml'
sed -i "s:^    <version>\(.*\)</version>:    <version>$VERSION</version>:" pom.xml

echo 'Updating ParameterUtil.scala'
sed -i "s:head(\"RMLStreamer\", \"\(.*\)\"):head(\"RMLStreamer\", \"$VERSION\"):" src/main/scala/io/rml/framework/core/util/ParameterUtil.scala

echo 'Updating README.md'
sed -i "s/rmlstreamer:\(.*\) toFile/rmlstreamer:$VERSION toFile/" README.md
