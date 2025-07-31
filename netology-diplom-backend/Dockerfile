FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY target/netology-diplom-backend-0.0.1-SNAPSHOT.jar app.jar

ENV JAVA_OPTS="-XX:ErrorFile=/tmp/java_error.log \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/tmp \
    -XX:+CrashOnOutOfMemoryError \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75"

ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
