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

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    implementation("org.springframework.cloud:spring-cloud-starter-config:4.2.2")  // Для взаимодействия с Config Server
    implementation("org.springframework.cloud:spring-cloud-starter-bus-amqp:4.2.1")// Для работы Spring Cloud Bus c RabbitM
// https://mvnrepository.com/artifact/org.springframework.cloud/spring-cloud-starter-netflix-eureka-client
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:4.2.1")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    compileOnly("org.projectlombok:lombok")


    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")

    testAnnotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mapstruct:mapstruct:1.5.5.Final")
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