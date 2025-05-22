package ru.muravin.bankapplication.cashInCashOutService.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import ru.muravin.bankapplication.cashInCashOutService.controller.MainController;
import ru.muravin.bankapplication.cashInCashOutService.dto.HttpResponseDto;
import ru.muravin.bankapplication.cashInCashOutService.dto.OperationDto;
import ru.muravin.bankapplication.cashInCashOutService.service.NotificationsServiceClient;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MainController.class)
public class MainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RestTemplate restTemplate;

    @MockitoBean
    private NotificationsServiceClient notificationsServiceClient;

    @Autowired
    private ObjectMapper objectMapper;

    private OperationDto operationDto;

    @BeforeEach
    void setUp() {
        operationDto = new OperationDto();
        operationDto.setAction("GET");
        operationDto.setLogin("john_doe");
        operationDto.setCurrencyCode("USD");
        operationDto.setAmount("100");
    }

    @Test
    void withdrawCash_ActionGET_ReturnsSuccessMessage() throws Exception {
        // Given
        when(restTemplate.postForObject(anyString(), any(OperationDto.class), eq(HttpResponseDto.class)))
                .thenReturn(new HttpResponseDto("OK", ""));
        when(restTemplate.postForObject(eq("http://gateway/accountsService/cashInOrCashOut"), any(OperationDto.class), eq(String.class)))
                .thenReturn("OK");

        // When & Then
        mockMvc.perform(post("/withdrawCash")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(operationDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("OK"))
                .andExpect(jsonPath("$.statusMessage").value("Деньги успешно списаны"));
    }

    @Test
    void withdrawCash_ActionPUT_ReturnsSuccessMessage() throws Exception {
        operationDto.setAction("PUT");

        when(restTemplate.postForObject(anyString(), any(OperationDto.class), eq(HttpResponseDto.class)))
                .thenReturn(new HttpResponseDto("OK", ""));
        when(restTemplate.postForObject(eq("http://gateway/accountsService/cashInOrCashOut"), any(OperationDto.class), eq(String.class)))
                .thenReturn("OK");

        mockMvc.perform(post("/withdrawCash")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(operationDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("OK"))
                .andExpect(jsonPath("$.statusMessage").value("Деньги успешно зачислены"));
    }

    @Test
    void withdrawCash_AntifraudReturnsError_ShouldPropagateError() throws Exception {
        when(restTemplate.postForObject(anyString(), any(OperationDto.class), eq(HttpResponseDto.class)))
                .thenReturn(new HttpResponseDto("ERROR", "Antifraud blocked"));

        mockMvc.perform(post("/withdrawCash")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(operationDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("ERROR"))
                .andExpect(jsonPath("$.statusMessage").value("Antifraud blocked"));
    }

    @Test
    void withdrawCash_AccountsServiceReturnsError_ShouldReturnError() throws Exception {
        when(restTemplate.postForObject(anyString(), any(OperationDto.class), eq(HttpResponseDto.class)))
                .thenReturn(new HttpResponseDto("OK", ""));
        when(restTemplate.postForObject(eq("http://gateway/accountsService/cashInOrCashOut"), any(OperationDto.class), eq(String.class)))
                .thenReturn("Ошибка перевода");

        mockMvc.perform(post("/withdrawCash")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(operationDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("ERROR"))
                .andExpect(jsonPath("$.statusMessage").value("Ошибка перевода"));
    }

    @Test
    void withdrawCash_ExceptionThrown_ShouldReturnErrorDto() throws Exception {
        when(restTemplate.postForObject(anyString(), any(OperationDto.class), eq(HttpResponseDto.class)))
                .thenThrow(new RuntimeException("Internal error"));

        mockMvc.perform(post("/withdrawCash")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(operationDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("ERROR"))
                .andExpect(jsonPath("$.statusMessage").value("Internal error"));
    }
}