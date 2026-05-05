FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY PaymentService/pom.xml PaymentService/pom.xml
COPY PaymentService/src PaymentService/src

RUN mvn -B -f PaymentService/pom.xml clean package -DskipTests -Djacoco.skip=true

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /workspace/PaymentService/target/*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
