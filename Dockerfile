# ------------------------------------------------------
# Build stage: Maven + Java 21
# ------------------------------------------------------
FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /workspace

# Cache dependencies
COPY pom.xml mvnw* ./
COPY .mvn .mvn
RUN mvn -B -q -e -DskipTests dependency:go-offline

# Copy source
COPY src ./src
RUN mvn -B -DskipTests package

# ------------------------------------------------------
# Runtime stage: Lightweight Java 21
# ------------------------------------------------------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy application JAR
ARG JAR_FILE=target/*.jar
COPY --from=build /workspace/${JAR_FILE} app.jar

# -------------------------------
# Runtime configuration
# -------------------------------

# JVM tuning (can be overridden from Render)
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# AWS SDK will AUTO-read these at runtime
# (DO NOT set values here – Render injects them)
ENV AWS_ACCESS_KEY_ID=""
ENV AWS_SECRET_ACCESS_KEY=""
ENV AWS_REGION=""
ENV AWS_DEFAULT_REGION=""

# Optional but useful for logging/debugging
ENV SPRING_PROFILES_ACTIVE=prod
ENV TZ=UTC

# Render provides $PORT dynamically
EXPOSE 8080

# -------------------------------
# Startup
# -------------------------------
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
