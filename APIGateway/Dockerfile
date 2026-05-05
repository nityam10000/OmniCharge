FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY APIGateway/pom.xml APIGateway/pom.xml
COPY APIGateway/src APIGateway/src

RUN mvn -B -f APIGateway/pom.xml clean package -DskipTests -Djacoco.skip=true

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /workspace/APIGateway/target/*.jar app.jar

EXPOSE 8087

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
