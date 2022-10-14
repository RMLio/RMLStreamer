#!/usr/bin/env bash

version=$(grep -F '<version>' pom.xml | head -n 1 | cut -d '>' -f 2 | cut -d '<' -f 1)

help() {
	echo "Build and push Docker images for RMLStreamer"
	echo
	echo "buildDocker.sh [-h]"
	echo "buildDocker.sh [-a][-n][-p][-u <username>][-v <version>]"
	echo "options:"
	echo "-a   Build for platforms linux/arm64 and linux/amd64. Default: perform a standard 'docker build'"
	echo "-h   Print this help and exit."
	echo "-n   Do NOT (re)build RMLStreamer before building the Docker image. This is risky because the Docker build needs a stand-alone version of RMLStreamer."
	echo "-u <username>  Add an username name to the tag name as on Docker Hub, like <username>/rmlstreamer:<version>."
	echo "-p   Push to Docker Hub repo. You must be logged in for this to succeed."
	echo "-v <version>       Override the version in the tag name, like <username>/rmlstreamer:<version>. If not given, use the current version found in pom.xml."
}

do_not_build=false
build_for_all=false
push=false


while getopts ahnu:pv: option
do
	case "${option}" in
		a)	# Build for all available OS/arcghitecture of base image
				build_for_all=true;;
		h)	# dislplay help
				help
				exit;;
		n)	# Do NOT (re)build RMLStreamer
				do_not_build=true;;
		u)	# Override username
				username=${OPTARG};;
		p)	# Push to Docker Hub
				push=true;;
		v)	# Override version
				version=${OPTARG};;
		\?) # Invalid option
				echo "Error: invalid option"
				exit;;
	esac
done

if [[ -z "$version" ]]
then
	version=$(grep -F '<version>' pom.xml | head -n 1 | cut -d '>' -f 2 | cut -d '<' -f 1)
fi

if [[ -n "$username" ]]
then
	username="${username}/"
fi

tag="${username}rmlstreamer:${version}"
tag_latest="${username}rmlstreamer:latest"
echo "tag: $tag"

if ! $do_not_build
then
	### 1. Build RMLStreamer stand-alone
	echo "Building stand-alone RMLStreamer"
	mvn clean package -DskipTests -P 'stand-alone'
fi


### 2. Build the docker container
echo "Building Docker image(s)"
if $build_for_all
then
	platforms="linux/arm64,linux/amd64"
	if $push
	then
		DOCKER_BUILDX_BUILD_ARGS=(--platform $platforms --tag $tag --tag $tag_latest --push .)
	else
		DOCKER_BUILDX_BUILD_ARGS=(--platform $platforms --tag $tag --tag $tag_latest .)
	fi
	docker buildx create --name rmlstreamerallplatforms --use
	docker buildx build ${DOCKER_BUILDX_BUILD_ARGS[@]}
	docker buildx rm rmlstreamerallplatforms
else
	docker build --tag $tag --tag $tag_latest .
	if $push
	then
		docker push $tag
		docker push $tag_latest
	fi
fi
