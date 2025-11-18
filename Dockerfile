FROM eclipse-temurin:21-jdk

WORKDIR /app

RUN groupadd -g 1000 spring && useradd -u 1000 -g spring -r spring

RUN mkdir -p /app/logs && chown -R spring:spring /app

COPY target/service-0.0.1-SNAPSHOT.jar app.jar

USER spring

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]