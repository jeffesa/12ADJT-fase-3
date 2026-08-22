# ----------- Build Stage -----------
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

COPY ../pom.xml ./pom.xml
COPY scheduling-service/pom.xml ./scheduling-service/pom.xml
COPY notification-service/pom.xml ./notification-service/pom.xml
COPY history-service/pom.xml ./history-service/pom.xml

RUN mvn dependency:go-offline -pl notification-service -am

COPY notification-service/src ./notification-service/src
RUN mvn clean package -pl notification-service -am -DskipTests

# ----------- Runtime Stage -----------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=build /app/notification-service/target/*.jar app.jar

ENV PORT=8082
EXPOSE 8082

ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=$PORT"]
