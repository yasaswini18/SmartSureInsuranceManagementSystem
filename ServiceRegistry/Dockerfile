FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/ServiceRegistry-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8761

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]