package ru.muravin.bankapplication.accountsService.controllerTest;
/*

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.muravin.bankapplication.notificationsService.controller.NotificationsController;
import ru.muravin.bankapplication.notificationsService.dto.NotificationDto;
import ru.muravin.bankapplication.notificationsService.service.NotificationsService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = NotificationsController.class)
public class NotificationsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationsService notificationsService;

    @Autowired
    private ObjectMapper objectMapper;

    private NotificationDto notificationDto;

    @BeforeEach
    void setUp() {
        notificationDto = new NotificationDto();
        notificationDto.setMessage("Test message");
        notificationDto.setSender("sender");
    }

    @Test
    void createNotification_ShouldReturnCreatedStatusAndId() throws Exception {
        // Given
        Long expectedId = 1L;
        when(notificationsService.sendNotification(any(NotificationDto.class))).thenReturn(expectedId);

        // When & Then
        mockMvc.perform(post("/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notificationDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value("OK"))
                .andExpect(jsonPath("$.statusMessage").value("Notification created"))
                .andExpect(jsonPath("$.notificationId").value(expectedId));

        verify(notificationsService, times(1)).sendNotification(any(NotificationDto.class));
    }

    @Test
    void createNotification_WhenServiceThrowsException_ShouldReturnError() throws Exception {
        // Given
        when(notificationsService.sendNotification(any(NotificationDto.class))).thenThrow(new RuntimeException("Internal error"));

        // When & Then
        mockMvc.perform(post("/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notificationDto)))
                .andExpect(status().isOk()) // Так как обработчик возвращает 200 с сообщением об ошибке
                .andExpect(jsonPath("$.statusCode").value("ERROR"))
                .andExpect(jsonPath("$.statusMessage").value("Internal error"))
                .andExpect(jsonPath("$.notificationId").doesNotExist());

        verify(notificationsService, times(1)).sendNotification(any(NotificationDto.class));
    }
}*/
