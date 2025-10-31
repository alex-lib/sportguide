FROM openjdk:21-oracle

WORKDIR /app

COPY target/service-0.0.1-SNAPSHOT.jar app.jar

# Создаем пользователя для безопасности
RUN groupadd -r spring && useradd -r -g spring spring
USER spring

#CMD ["java", "-jar", "app.jar"]
RUN mkdir -p /app/logs && chmod -R 777 /app/logs
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]