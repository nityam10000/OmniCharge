FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY OperatorPlanManagement/pom.xml OperatorPlanManagement/pom.xml
COPY OperatorPlanManagement/src OperatorPlanManagement/src

RUN mvn -B -f OperatorPlanManagement/pom.xml clean package -DskipTests -Djacoco.skip=true

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /workspace/OperatorPlanManagement/target/*.jar app.jar

EXPOSE 8086

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
