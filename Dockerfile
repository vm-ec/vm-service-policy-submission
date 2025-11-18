# ---- Build stage: Maven with Java 21 ----
FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /workspace

# copy maven wrapper and pom first for better caching
COPY pom.xml mvnw* ./
COPY .mvn .mvn
COPY src ./src

RUN mvn -B -DskipTests package

# ---- Runtime stage: lightweight Java 21 JRE ----
FROM eclipse-temurin:21-jre
WORKDIR /app

ARG JAR_FILE=target/*.jar
COPY --from=build /workspace/${JAR_FILE} app.jar

ENV JAVA_OPTS="-Xmx512m"

# Render provides $PORT; application.properties uses server.port=${PORT:8080}
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
