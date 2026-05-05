FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY UserManagement/pom.xml UserManagement/pom.xml
COPY UserManagement/src UserManagement/src

RUN mvn -B -f UserManagement/pom.xml clean package -DskipTests -Djacoco.skip=true

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /workspace/UserManagement/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
