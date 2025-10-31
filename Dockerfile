FROM openjdk:21-oracle

# Рабочая директория
WORKDIR /app

# Копируем jar от root
COPY target/service-0.0.1-SNAPSHOT.jar app.jar

# Создаем пользователя и группу
RUN groupadd -r spring && useradd -r -g spring spring

# Создаем директорию логов и даем права пользователю spring
RUN mkdir -p /app/logs && chown -R spring:spring /app

# Переключаемся на пользователя
USER spring

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]
