# syntax=docker/dockerfile:1
ARG BASE_JDK_IMAGE=amazoncorretto:21-alpine

ARG TINI_VERSION=v0.19.0

ARG WORKDIR=/project

ARG SKIP_TESTS=true

FROM ${BASE_JDK_IMAGE} as download-dependencies

ARG WORKDIR

WORKDIR ${WORKDIR}

COPY ./pom.xml ./mvnw ./mvnw.cmd .
COPY ./.mvn ./.mvn
COPY ./service/pom.xml ./service/pom.xml

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B dependency:go-offline -T 2C -DskipTests=true && \
    true

FROM download-dependencies as builder

ARG SKIP_TESTS

COPY ./service/src ./service/src

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B package -T 2C -DskipTests=${SKIP_TESTS} && \
    true

FROM ${BASE_JDK_IMAGE} as runner

ARG WORKDIR

RUN apk add --no-cache tini

ARG SERVICE_EXECUTABLE=${WORKDIR}/service/target/service-*.jar

COPY --from=builder ${SERVICE_EXECUTABLE} /comminucator-vm.jar

ENTRYPOINT [ "/sbin/tini", "--"]
CMD [ "java", "-jar", "/comminucator-vm.jar" ]
