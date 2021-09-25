FROM ubuntu:latest as ScalaBuild

EXPOSE 3000 3000
EXPOSE 8080

SHELL ["/bin/bash", "-c"]

ENV JAVA_VER 8.0.292.hs-adpt
ENV SCALA_VER 2.12.7
ENV SBT_VER 1.2.8

RUN mkdir -pv /persist
VOLUME /persist

RUN apt-get update && apt-get upgrade -y
RUN apt-get update && apt-get install curl bash unzip zip sudo -y
RUN curl -fsSL https://deb.nodesource.com/setup_15.x | sudo -E bash - && sudo apt-get install -y nodejs
RUN curl -s "https://get.sdkman.io" | bash
RUN rm /bin/sh && ln -s /bin/bash /bin/sh
RUN source "$HOME/.sdkman/bin/sdkman-init.sh" && \
    sdk list java && \
    sdk install java ${JAVA_VER} && \
    sdk list scala && \
    sdk install scala ${SCALA_VER} && \
    sdk install sbt ${SBT_VER}

WORKDIR /app
COPY ./api/project /app/project
COPY ./api/src /app/src
COPY ./api/build.sbt /app

ENV PATH=/root/.sdkman/candidates/java/current/bin:$PATH
ENV PATH=/root/.sdkman/candidates/scala/current/bin:$PATH
ENV PATH=/root/.sdkman/candidates/sbt/current/bin:$PATH

RUN sbt compile && sbt package
CMD sbt run

