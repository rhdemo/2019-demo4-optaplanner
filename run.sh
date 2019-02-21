#!/usr/bin/env bash
IMAGE=optaplanner-demo:latest
CONTAINER_NAME=optaplanner-demo

docker kill $CONTAINER_NAME > .optaplanner-demo.log 2>&1
docker rm $CONTAINER_NAME > .optaplanner-demo.log 2>&1
docker run -d -p 8080:8080 --name $CONTAINER_NAME $IMAGE
