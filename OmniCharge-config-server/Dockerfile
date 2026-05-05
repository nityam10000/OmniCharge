FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY OmniCharge-config-server/pom.xml OmniCharge-config-server/pom.xml
COPY OmniCharge-config-server/src OmniCharge-config-server/src

RUN mvn -B -f OmniCharge-config-server/pom.xml clean package -DskipTests -Djacoco.skip=true

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /workspace/OmniCharge-config-server/target/*.jar app.jar

EXPOSE 9999

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
