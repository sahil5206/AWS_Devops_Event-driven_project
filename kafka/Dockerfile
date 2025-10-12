FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apt-get update && apt-get install -y maven
RUN mvn package -DskipTests

CMD ["java", "-cp", "target/kafka-microservice-1.0-SNAPSHOT.jar", "ProducerApp"]
