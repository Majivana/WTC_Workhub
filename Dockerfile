FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-jammy AS runtime

ENV JAVA_OPTS=""
ENV WTC_DB_PATH=/data/workhub.db
ENV WTC_STORAGE_PROVIDER=local
ENV WTC_STORAGE_LOCAL_DIRECTORY=/data/private-object-data

WORKDIR /app

RUN apt-get update \
    && apt-get install --no-install-recommends -y curl \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p /data \
    && useradd --system --uid 10001 --create-home --home-dir /home/workhub workhub \
    && chown -R workhub:workhub /app /data

COPY --from=build --chown=workhub:workhub /workspace/target/WTC_Workhub-1.0-SNAPSHOT.jar /app/app.jar

USER 10001:10001
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD curl --fail --silent http://127.0.0.1:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
