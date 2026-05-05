FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY AuthService/pom.xml AuthService/pom.xml
COPY AuthService/src AuthService/src

RUN mvn -B -f AuthService/pom.xml clean package -DskipTests -Djacoco.skip=true

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /workspace/AuthService/target/*.jar app.jar

EXPOSE 8089

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
