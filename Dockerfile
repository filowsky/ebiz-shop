#FROM hseeberger/scala-sbt:graalvm-ce-19.3.0-java11_1.3.7_2.13.1 as build
#COPY . /my-project
#WORKDIR /my-project
#RUN sbt assembly

FROM openjdk:8-jre-alpine
EXPOSE 8080 8080
WORKDIR /usr/src/myapp
ADD target/scala-2.12/http4s-practice-assembly-0.0.1-SNAPSHOT.jar app.jar
CMD java -jar app.jar