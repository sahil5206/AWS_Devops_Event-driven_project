FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY . .
RUN mvn -B package dependency:copy-dependencies -DincludeScope=runtime

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /build/target/kafka-microservice-1.0-SNAPSHOT.jar /app/app.jar
COPY --from=build /build/target/dependency /app/dependency
CMD ["java", "-cp", "/app/app.jar:/app/dependency/*", "ProducerApp"]
