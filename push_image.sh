#!/bin/bash
source ./.env

mvn clean install -Prelease \
  -Ddocker.username=$DOCKER_USERNAME -Ddocker.password=$DOCKER_PASSWORD