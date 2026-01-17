FROM gradle:9.3.0-jdk21 AS build

WORKDIR /app

COPY gradle ./gradle
COPY src ./src
COPY build.gradle .
COPY settings.gradle .

RUN gradle clean bootJar --no-daemon

FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]