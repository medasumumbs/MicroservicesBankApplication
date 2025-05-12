package ru.muravin.bankapplication.notificationsService;

import org.mapstruct.factory.Mappers;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import ru.muravin.bankapplication.transferService.TransferServiceApplication;

@TestConfiguration
public class TestApplicationConfiguration {

    @Bean
    public NotificationMapper notificationMapper() {
        return Mappers.getMapper(NotificationMapper.class);
    }
    public static void main(String[] args) {
        SpringApplication.from(TransferServiceApplication::main).with(TestContainersConfiguration.class).run(args);
    }
}
