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
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    implementation("org.springframework.cloud:spring-cloud-starter-config:4.2.2")  // Для взаимодействия с Config Server
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:4.2.1")
    implementation("io.micrometer:micrometer-tracing-bridge-brave")  // мост между micrometer и brave
    implementation("io.zipkin.reporter2:zipkin-reporter-brave") // библиотека для отправки спанов в zipkin
    implementation("io.zipkin.brave:brave-instrumentation-http:6.3.0")
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

    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")

    testAnnotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.mapstruct:mapstruct:1.5.5.Final")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.6.0")
    // https://mvnrepository.com/artifact/org.springframework.security/spring-security-test
    testImplementation("org.springframework.security:spring-security-test:6.5.0")
    // https://mvnrepository.com/artifact/io.projectreactor/reactor-test
    testImplementation("io.projectreactor:reactor-test:3.7.6")
}

tasks.withType<Test> {
    environment( "UI_SERVICE_PORT", "8087");
    environment( "SERVER_PORT","8087");
    useJUnitPlatform()
}