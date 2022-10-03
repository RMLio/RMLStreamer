#!/usr/bin/env bash

version=$(grep -F '<version>' pom.xml | head -n 1 | cut -d '>' -f 2 | cut -d '<' -f 1)

### 1. Build RMLStreamer stand-alone
echo "Building stand-alone RMLStreamer version $version"
mvn clean package -DskipTests -P 'stand-alone'

### 2. Build the docker container
docker build --tag "rmlstreamer:$version" . && \
echo "Successfully built rmlstreamer:$version !"
