package ru.muravin.bankapplication.notificationsService;

import org.mapstruct.factory.Mappers;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import ru.muravin.bankapplication.currencyExchangeService.CurrencyExchangeServiceApplication;
import ru.muravin.bankapplication.currencyExchangeService.mapper.CurrencyRateMapper;

@TestConfiguration
@SpringBootApplication
public class TestApplicationConfiguration {

    @Bean
    public CurrencyRateMapper notificationMapper() {
        return Mappers.getMapper(CurrencyRateMapper.class);
    }
    public static void main(String[] args) {
        SpringApplication.from(CurrencyExchangeServiceApplication::main).with(TestContainersConfiguration.class).run(args);
    }
}
