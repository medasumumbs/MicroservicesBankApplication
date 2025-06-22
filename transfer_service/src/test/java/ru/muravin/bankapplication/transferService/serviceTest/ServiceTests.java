package ru.muravin.bankapplication.transferService.serviceTest;

import kafka.Kafka;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;
import ru.muravin.bankapplication.transferService.TransferServiceApplication;
import ru.muravin.bankapplication.transferService.configuration.OAuth2SecurityConfig;
import ru.muravin.bankapplication.transferService.dto.NotificationDto;
import ru.muravin.bankapplication.transferService.service.NotificationsServiceClient;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThatException;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThatNoException;
@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration," +
                "org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration"
},
        classes = {TransferServiceApplication.class})
@TestPropertySource(locations = "classpath:application.yml")
@AutoConfigureWebTestClient
@ExtendWith(MockitoExtension.class)
public class ServiceTests {
    @MockitoBean
    private OAuth2SecurityConfig oAuth2SecurityConfig;

    @MockitoBean
    private JwtAuthenticationConverter jwtAuthenticationConverter;
    @MockitoBean
    private ApplicationContext applicationContext;

    @MockitoBean
    private KafkaTemplate<String, NotificationDto> kafkaTemplate;

    @Autowired
    private NotificationsServiceClient notificationsServiceClient;

    private String notificationText = "User registered john_doe";

    @BeforeEach
    void setUp() {
        when(applicationContext.getApplicationName()).thenReturn("accounts-service");
    }

    @Test
    void sendNotification_ShouldCallKafkaTemplateWithMessage() {
        // Given
        when(applicationContext.getApplicationName()).thenReturn("accounts-service");

        when(kafkaTemplate.send(any(Message.class))).thenReturn(CompletableFuture.completedFuture(new SendResult<>(null, null)));

        // When
        notificationsServiceClient.sendNotification(notificationText, "123");

        // Then
        verify(kafkaTemplate, times(1)).send(any(Message.class));
    }

    @Test
    void sendNotification_WhenRestTemplateThrowsException_ShouldNotFail() {
        // Given
        doThrow(new RuntimeException("Connection refused"))
                .when(kafkaTemplate).send(any(Message.class));

        // When & Then
        assertThatException()
                .isThrownBy(() -> notificationsServiceClient.sendNotification(notificationText, "123"));

        // Проверяем, что RestTemplate был вызван
        verify(kafkaTemplate, times(1)).send(any(Message.class));
    }
}