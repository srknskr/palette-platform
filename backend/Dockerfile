FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace
COPY gradlew gradlew
COPY gradle gradle
COPY settings.gradle.kts build.gradle.kts gradle.properties detekt.yml ./
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon
COPY src src
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:17-jre
RUN useradd --system --uid 10001 palette
WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar app.jar
USER palette
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=3s --retries=5 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
