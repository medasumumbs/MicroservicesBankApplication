plugins {
    java
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "ru.practicum"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

// Для переключения LogBack->Log4j2
configurations.configureEach {
    exclude(module = "spring-boot-starter-logging")
    exclude(group = "ch.qos.logback")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("org.springframework.cloud:spring-cloud-starter-gateway:4.2.2")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

    implementation("org.springframework.cloud:spring-cloud-gateway-webflux:4.2.2")
    implementation("org.springframework.cloud:spring-cloud-starter-config:4.2.2")  // Для взаимодействия с Config Server

    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:4.2.1")
    implementation("io.micrometer:micrometer-tracing-bridge-brave")  // мост между micrometer и brave
    implementation("io.zipkin.reporter2:zipkin-reporter-brave") // библиотека для отправки спанов в zipkin
    compileOnly("org.projectlombok:lombok")

    /// Метрики для prometheus
    implementation("io.micrometer:micrometer-registry-prometheus")

    /// Логирование
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.24.3")
    implementation("org.apache.logging.log4j:log4j-core:2.24.3")
    implementation("org.apache.logging.log4j:log4j-api:2.24.3")
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    /// EndOf: Логирование
    implementation("org.springframework.kafka:spring-kafka")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}