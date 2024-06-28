FROM gradle:8-alpine as builder

COPY . /app
RUN cd /app && gradle --no-daemon buildFatJar

FROM bellsoft/liberica-runtime-container:jre-slim as runner

EXPOSE 8080
VOLUME [ "/app/opds.db", "/library/" ]
WORKDIR /app
ENTRYPOINT java -jar opdsko.jar -P:ktor.indexer.path=/library
RUN mkdir -p /library
COPY --from=builder /app/build/libs/*.jar /app/opdsko.jar

