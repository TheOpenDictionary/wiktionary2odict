#!/usr/bin/env bash

DOCKER_NAME="wiki2odict"

if [ "$#" -eq 0 ]; then
    echo "Usage: ./docker.sh [target_language_code]"
else
    docker rmi -f $DOCKER_NAME
    docker rm -f $DOCKER_NAME
    docker build -t $DOCKER_NAME .
    docker run -e lang=$1 --name ${DOCKER_NAME} -d -it ${DOCKER_NAME}
fi
