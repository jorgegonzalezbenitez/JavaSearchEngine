FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY target/Query-Engine-1.0-SNAPSHOT.jar /app/Query-Engine-1.0-SNAPSHOT.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "Query-Engine-1.0-SNAPSHOT.jar"]