FROM maven:3.9.9-eclipse-temurin-11 AS build

WORKDIR /workspace

COPY pom.xml .
COPY src ./src

RUN mvn -DskipTests clean package

FROM tomcat:9.0-jdk11-temurin

WORKDIR /usr/local/tomcat/webapps

COPY --from=build /workspace/target/*.war ROOT.war

EXPOSE 8080