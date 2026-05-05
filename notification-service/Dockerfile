FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY notification-service/pom.xml notification-service/pom.xml
COPY notification-service/src notification-service/src

RUN mvn -B -f notification-service/pom.xml clean package -DskipTests -Djacoco.skip=true

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /workspace/notification-service/target/*.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
