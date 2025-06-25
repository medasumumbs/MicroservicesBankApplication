

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
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    implementation("org.springframework.cloud:spring-cloud-starter-config:4.2.2")  // Для взаимодействия с Config Server
    //implementation("org.springframework.cloud:spring-cloud-starter-bus-amqp:4.2.1")// Для работы Spring Cloud Bus c RabbitM
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:4.2.1")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("com.fasterxml.jackson.core:jackson-databind")
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

    runtimeOnly("org.postgresql:postgresql")

    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")

    testAnnotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.apache.kafka:kafka-clients")
    testImplementation("org.apache.kafka:kafka_2.13")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.mapstruct:mapstruct:1.5.5.Final")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("com.redis:testcontainers-redis:2.2.2")
    // https://mvnrepository.com/artifact/org.mockito/mockito-core
    testImplementation("org.mockito:mockito-core:5.6.0")
}

tasks.withType<Test> {
    environment("NOTIFICATIONS_SERVICE_DB_HOST", "localhost");
    environment( "NOTIFICATIONS_SERVICE_DB_NAME", "notifications");
    environment( "NOTIFICATIONS_SERVICE_DB_USERNAME", "myuser");
    environment( "NOTIFICATIONS_SERVICE_DB_PASSWORD", "secret");
    environment( "NOTIFICATIONS_SERVICE_PORT", "8081");

    environment( "SPRING_DATASOURCE_URL","jdbc:postgresql://localhost:5433/notifications");
    environment( "SPRING_DATASOURCE_USERNAME", "myuser");
    environment( "SPRING_DATASOURCE_PASSWORD","secret");
    environment( "SERVER_PORT","8081");
    useJUnitPlatform()
}