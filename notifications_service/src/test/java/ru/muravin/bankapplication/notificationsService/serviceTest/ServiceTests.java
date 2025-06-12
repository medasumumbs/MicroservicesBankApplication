package ru.muravin.bankapplication.notificationsService.serviceTest;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mapstruct.Context;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockReset;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.A;
import org.testcontainers.utility.TestcontainersConfiguration;
import ru.muravin.bankapplication.notificationsService.NotificationsServiceApplication;
import ru.muravin.bankapplication.notificationsService.TestApplicationConfiguration;
import ru.muravin.bankapplication.notificationsService.configurations.OAuth2SecurityConfig;
import ru.muravin.bankapplication.notificationsService.dto.NotificationDto;
import ru.muravin.bankapplication.notificationsService.mapper.NotificationMapper;
import ru.muravin.bankapplication.notificationsService.mapper.NotificationMapperImpl;
import ru.muravin.bankapplication.notificationsService.model.Notification;
import ru.muravin.bankapplication.notificationsService.repository.NotificationsRepository;
import ru.muravin.bankapplication.notificationsService.service.NotificationsService;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=update",
        "spring.liquibase.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration," +
        "org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration"
        },
        classes = {TestApplicationConfiguration.class, NotificationsServiceApplication.class})
@TestPropertySource(locations = "classpath:application.yml")
@AutoConfigureWebTestClient
@ExtendWith(MockitoExtension.class)
public class ServiceTests {
    @MockitoBean
    private OAuth2SecurityConfig oAuth2SecurityConfig;

    @MockitoBean
    private JwtAuthenticationConverter jwtAuthenticationConverter;

    @MockitoBean
    private NotificationMapper notificationMapper;

    @Autowired
    private NotificationsService notificationsService;

    @Autowired
    private NotificationsRepository notificationsRepository;

    @Test
    void testSave() {

        LocalDateTime localDateTime = LocalDateTime.now();
        NotificationDto notificationDto = new NotificationDto("abcde","a",new Timestamp(System.currentTimeMillis()).toString());
        // Создаём ожидаемую сущность
        Notification expectedEntity = new Notification();
        expectedEntity.setMessage("abcde");
        expectedEntity.setSender("a");
        expectedEntity.setCreatedAt(localDateTime);

        // Настраиваем мок, чтобы он возвращал эту сущность
        when(notificationMapper.toEntity(notificationDto)).thenReturn(expectedEntity);

        notificationsRepository.deleteAll();

        notificationsService.sendNotification(notificationDto);
        var notification = notificationsRepository.findAll().get(0);
        assertEquals("abcde", notification.getMessage());
        assertEquals("a", notification.getSender());
        assertEquals(localDateTime.getMinute(),notification.getCreatedAt().getMinute());
    }
}
