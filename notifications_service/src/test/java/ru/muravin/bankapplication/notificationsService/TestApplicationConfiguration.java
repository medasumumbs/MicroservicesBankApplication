package ru.muravin.bankapplication.notificationsService;

import org.mapstruct.factory.Mappers;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.testcontainers.utility.TestcontainersConfiguration;
import ru.muravin.bankapplication.notificationsService.mapper.NotificationMapper;

@TestConfiguration
public class TestApplicationConfiguration {

    @Bean
    public NotificationMapper notificationMapper() {
        return Mappers.getMapper(NotificationMapper.class);
    }
}
