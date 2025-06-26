FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

RUN apk add --no-cache ffmpeg   # no apt ... -> apk (alpine package keeper) correct  ... -> Or run FFmpeg in a separate container
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
