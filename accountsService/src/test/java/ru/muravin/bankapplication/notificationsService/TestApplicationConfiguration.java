package ru.muravin.bankapplication.notificationsService;

import org.mapstruct.factory.Mappers;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import ru.muravin.bankapplication.accountsService.AccountsServiceApplication;
import ru.muravin.bankapplication.accountsService.mapper.UserMapper;

@TestConfiguration
public class TestApplicationConfiguration {

    @Bean
    public UserMapper notificationMapper() {
        return Mappers.getMapper(UserMapper.class);
    }
    public static void main(String[] args) {
        SpringApplication.from(AccountsServiceApplication::main).with(TestContainersConfiguration.class).run(args);
    }
}
