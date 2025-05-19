package ru.muravin.bankapplication.notificationsService.serviceTest;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.utility.TestcontainersConfiguration;
import ru.muravin.bankapplication.currencyExchangeService.dto.CurrencyRateDto;
import ru.muravin.bankapplication.currencyExchangeService.service.CurrencyRatesService;
import ru.muravin.bankapplication.notificationsService.TestApplicationConfiguration;
import ru.muravin.bankapplication.currencyExchangeService.mapper.CurrencyRateMapper;
import ru.muravin.bankapplication.currencyExchangeService.repository.CurrencyRatesRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*@SpringBootTest(classes = {TestApplicationConfiguration.class})
@Import(TestcontainersConfiguration.class)
@ContextConfiguration(classes = TestApplicationConfiguration.class)
@TestPropertySource(locations = "classpath:application.yml")
@AutoConfigureWebTestClient
@ExtendWith(MockitoExtension.class)*/
public class ServiceTests {
    /*@MockitoBean
    private CurrencyRateMapper notificationMapper;

    @Autowired
    private CurrencyRatesService notificationsService;

    @Autowired
    private CurrencyRatesRepository notificationsRepository;

    @Test
    @Disabled
    void testSave() {
        notificationsRepository.deleteAll();
        var localDateTime = LocalDateTime.now();
        CurrencyRateDto notificationDto = new CurrencyRateDto("abcde","a",localDateTime);
        notificationsService.sendNotification(notificationDto);
        var notification = notificationsRepository.findAll().get(0);
        assertEquals("abcde", notification.getMessage());
        assertEquals("a", notification.getSender());
        assertEquals(localDateTime,notification.getCreatedAt());
    }*/
}
