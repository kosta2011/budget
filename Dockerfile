FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Явное имя JAR (после `mvn clean package` будет создан budget-0.0.1-SNAPSHOT.jar)
COPY target/budget-0.0.1-SNAPSHOT.jar app.jar

RUN chown -R appuser:appgroup /app

USER appuser

ENTRYPOINT ["java", "-jar", "app.jar"]