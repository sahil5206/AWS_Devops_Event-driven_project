FROM openjdk:17-jdk-slim
WORKDIR /app
COPY . /app
RUN mkdir -p /app/libs
RUN javac ProducerApp.java ConsumerApp.java
CMD ["java", "ProducerApp"]
