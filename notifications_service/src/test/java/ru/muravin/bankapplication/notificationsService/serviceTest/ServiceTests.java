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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockReset;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.A;
import org.testcontainers.utility.TestcontainersConfiguration;
import ru.muravin.bankapplication.notificationsService.NotificationsServiceApplication;
import ru.muravin.bankapplication.notificationsService.TestApplicationConfiguration;
import ru.muravin.bankapplication.notificationsService.dto.NotificationDto;
import ru.muravin.bankapplication.notificationsService.mapper.NotificationMapper;
import ru.muravin.bankapplication.notificationsService.mapper.NotificationMapperImpl;
import ru.muravin.bankapplication.notificationsService.model.Notification;
import ru.muravin.bankapplication.notificationsService.repository.NotificationsRepository;
import ru.muravin.bankapplication.notificationsService.service.NotificationsService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {TestApplicationConfiguration.class})
@Import(TestcontainersConfiguration.class)
@ContextConfiguration(classes = TestApplicationConfiguration.class)
@TestPropertySource(locations = "classpath:application.yml")
@AutoConfigureWebTestClient
@ExtendWith(MockitoExtension.class)
@Disabled
public class ServiceTests {
    @MockitoBean
    private NotificationMapper notificationMapper;

    @Autowired
    private NotificationsService notificationsService;

    @Autowired
    private NotificationsRepository notificationsRepository;

    @Test
    @Disabled
    void testSave() {
        notificationsRepository.deleteAll();
        var localDateTime = LocalDateTime.now();
        NotificationDto notificationDto = new NotificationDto("abcde","a",localDateTime);
        notificationsService.sendNotification(notificationDto);
        var notification = notificationsRepository.findAll().get(0);
        assertEquals("abcde", notification.getMessage());
        assertEquals("a", notification.getSender());
        assertEquals(localDateTime,notification.getCreatedAt());
    }
}
