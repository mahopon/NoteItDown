FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2/repository mvn dependency:go-offline -B
COPY src src
RUN --mount=type=cache,target=/root/.m2/repository mvn package -DskipTests -B

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd --create-home --shell /usr/sbin/nologin app \
    && wget --progress=dot:giga -O otel.java https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar

COPY --from=builder /build/target/*.jar app.jar
RUN chown app:app app.jar otel.java

USER app

EXPOSE 8080
ENTRYPOINT ["java", "-javaagent:./otel.java", "-jar", "app.jar"]