# ========== STAGE 1: Build Spring Boot JAR ==========
FROM gradle:8.7-jdk17-alpine AS builder

WORKDIR /build

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# download dependencies
RUN ./gradlew dependencies --no-daemon || true

# copy source code
COPY src src

# build jar
RUN ./gradlew clean bootJar --no-daemon

# ========== STAGE 2: Runtime image ==========
FROM eclipse-temurin:17-jdk-alpine

# install bash
RUN apk add --no-cache bash

WORKDIR /app

# copy jar file
COPY --from=builder /build/build/libs/*.jar app.jar

COPY wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

EXPOSE 8080

# ENTRYPOINT ["java", "-jar", "app.jar"]
ENTRYPOINT ["/wait-for-it.sh", "postgres:5432", "--timeout=60", "--strict", "--", "java", "-jar", "app.jar"]