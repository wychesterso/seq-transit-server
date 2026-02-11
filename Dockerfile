FROM eclipse-temurin:17-jdk-alpine

# install bash
RUN apk add --no-cache bash

# copy jar (created with ./gradlew clean bootJar)
WORKDIR /app
COPY build/libs/seq_transit.jar app.jar

COPY wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

EXPOSE 8080

# ENTRYPOINT ["java", "-jar", "app.jar"]
ENTRYPOINT ["/wait-for-it.sh", "postgres:5432", "--timeout=60", "--strict", "--", "java", "-jar", "app.jar"]