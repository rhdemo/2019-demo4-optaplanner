FROM openjdk:8-jdk-alpine
MAINTAINER Radovan Synek <rsynek@redhat.com>

ARG JAR_PATH

VOLUME /tmp
COPY ${JAR_PATH} app.jar

EXPOSE 8080

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
