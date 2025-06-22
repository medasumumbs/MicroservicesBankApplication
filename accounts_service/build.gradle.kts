import org.springframework.cloud.contract.verifier.config.TestFramework

plugins {
    java
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.springframework.cloud.contract") version "4.2.1"
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

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    implementation("org.springframework.cloud:spring-cloud-starter-config:4.2.2")  // Для взаимодействия с Config Server
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:4.2.1")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("io.micrometer:micrometer-tracing-bridge-brave")  // мост между micrometer и brave
    implementation("io.zipkin.reporter2:zipkin-reporter-brave") // библиотека для отправки спанов в zipkin
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:7.3")
    compileOnly("org.projectlombok:lombok")

    /// Метрики для prometheus
    implementation("io.micrometer:micrometer-registry-prometheus")

    runtimeOnly("org.postgresql:postgresql")

    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")

    testAnnotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mapstruct:mapstruct:1.5.5.Final")


    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito:mockito-core:5.6.0")
    testImplementation("com.h2database:h2")
    testImplementation("org.springframework.cloud:spring-cloud-starter-contract-verifier:4.2.1")
    testImplementation("org.springframework.kafka:spring-kafka-test")
}

tasks.withType<Test> {
    environment("ACCOUNTS_SERVICE_DB_HOST", "localhost");
    environment( "ACCOUNTS_SERVICE_DB_NAME", "accounts");
    environment( "ACCOUNTS_SERVICE_DB_USERNAME", "myuser");
    environment( "ACCOUNTS_SERVICE_DB_PASSWORD", "secret");
    environment( "ACCOUNTS_SERVICE_PORT", "8082");

    environment( "SPRING_DATASOURCE_URL","jdbc:postgresql://localhost:5434/accounts");
    environment( "SPRING_DATASOURCE_USERNAME", "myuser");
    environment( "SPRING_DATASOURCE_PASSWORD","secret");
    environment( "SERVER_PORT","8084");
    useJUnitPlatform()
}
contracts {
    baseClassForTests.set("ru.muravin.bankapplication.accountsService.BaseContractTest"); // Указание базового класса для тестов
    contractsDslDir.set(file("src/test/resources/contracts"));
    testFramework.set(TestFramework.JUNIT5)
}