FROM openjdk:8-jdk-alpine
MAINTAINER Radovan Synek <rsynek@redhat.com>

ENV STI_SCRIPTS_URL=image:///usr/libexec/s2i
ENV STI_SCRIPTS_PATH=/usr/libexec/s2i
ENV APP_ROOT=/opt/app-root
ENV HOME=${APP_ROOT}/src


LABEL io.openshift.s2i.scripts-url=${STI_SCRIPTS_URL} \
      io.s2i.scripts-url=${STI_SCRIPTS_URL}

RUN apk --update add \
    bash \
    maven \
    && rm -rf /var/lib/apt/lists/* \
    && rm /var/cache/apk/*

RUN mkdir -p /usr/libexec/s2i

COPY ./s2i/bin/ ${STI_SCRIPTS_PATH}

RUN mkdir -p ${HOME}
WORKDIR ${HOME}

COPY ./ ${HOME}

EXPOSE 8080

CMD $STI_SCRIPTS_PATH/usage
