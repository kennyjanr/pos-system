# Since JAR is already built locally, just copy it for Docker runtime
# Requires: Local build first - cd backend && ./mvnw clean package
FROM alpine:latest
RUN apk add --no-cache openjdk17-jre
WORKDIR /app
COPY target/pos-system-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
