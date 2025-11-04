FROM openjdk:21-oracle

WORKDIR /app

# UID 1000 совпадает с обычным пользователем на Linux/WSL/macOS
RUN groupadd -g 1000 spring && useradd -u 1000 -g spring -r spring

# Создаем директорию логов и передаем права пользователю
RUN mkdir -p /app/logs && chown -R spring:spring /app

COPY target/service-0.0.1-SNAPSHOT.jar app.jar

USER spring

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]