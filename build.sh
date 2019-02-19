#!/usr/bin/env bash
./mvnw clean package && docker build \
  --build-arg JAR_PATH=target/optaplanner-demo.jar \
  --tag optaplanner-demo \
  "."
