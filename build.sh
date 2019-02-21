#!/usr/bin/env bash
mvn clean package && docker build \
  --build-arg JAR_PATH=target/optaplanner-demo.jar \
  --tag optaplanner-demo \
  "."
