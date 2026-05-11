# =============================================================================
# Stage 1 — Build the fat JAR
# =============================================================================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy pom first to leverage Docker layer cache for dependencies
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline

COPY src ./src
RUN mvn -B -ntp package -DskipTests

# =============================================================================
# Stage 2 — Slim runtime image
# =============================================================================
FROM eclipse-temurin:21-jre
WORKDIR /app

# Run as non-root
RUN groupadd --system app && useradd --system --gid app app

COPY --from=build /workspace/target/*.jar /app/app.jar
RUN chown -R app:app /app
USER app

EXPOSE 8080

ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
