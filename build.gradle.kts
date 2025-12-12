plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.0.0"
    id("org.jetbrains.kotlin.plugin.spring") version "2.0.0"
    id("org.springframework.boot") version "3.5.4"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "com.sport"
version = "0.0.1-SNAPSHOT"
description = "service"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

val springAiVersion = "1.0.1"

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:$springAiVersion")
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2025.0.0")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    implementation("org.springframework.retry:spring-retry")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.security:spring-security-config")
    implementation("org.springframework.security:spring-security-web")
//    implementation("org.springframework.security:spring-security-oauth2-client")
//    implementation("org.springframework.security:spring-security-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.security:spring-security-oauth2-jose")
    implementation("com.auth0:java-jwt:4.4.0")

    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-aop")

    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("com.google.guava:guava:32.0.1-jre")
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    implementation("org.apache.commons:commons-lang3:3.18.0")

    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")

    implementation("org.telegram:telegrambots:6.9.7.1")
    implementation("org.telegram:telegrambotsextensions:6.9.7.1")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}