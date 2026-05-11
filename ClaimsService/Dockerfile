FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Claims service needs uploads folder for documents
RUN mkdir -p /app/uploads/claims

COPY target/ClaimsService-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8083
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]