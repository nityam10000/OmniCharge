FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY UserManagement/pom.xml UserManagement/pom.xml
COPY UserManagement/src UserManagement/src
COPY RechargeProcessing/pom.xml RechargeProcessing/pom.xml
COPY RechargeProcessing/src RechargeProcessing/src

RUN mvn -B -f UserManagement/pom.xml clean install -DskipTests -Djacoco.skip=true
RUN mvn -B -f RechargeProcessing/pom.xml clean package -DskipTests -Djacoco.skip=true

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /workspace/RechargeProcessing/target/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
