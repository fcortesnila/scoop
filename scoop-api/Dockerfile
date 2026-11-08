FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S scoop && adduser -S scoop -G scoop
COPY --from=build /workspace/target/scoop-api-0.0.1-SNAPSHOT.jar app.jar
USER scoop
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
