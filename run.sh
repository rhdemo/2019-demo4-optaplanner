#!/usr/bin/env bash
IMAGE=optaplanner-demo:latest
CONTAINER_NAME=optaplanner-demo

docker rm $CONTAINER_NAME
docker run -d -p 8080:8080 --name $CONTAINER_NAME $IMAGE
