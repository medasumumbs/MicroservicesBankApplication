package ru.muravin.bankapplication.transferService.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import ru.muravin.bankapplication.transferService.controller.MainController;
import ru.muravin.bankapplication.transferService.dto.AccountInfoDto;
import ru.muravin.bankapplication.transferService.dto.CheckedTransferDto;
import ru.muravin.bankapplication.transferService.dto.HttpResponseDto;
import ru.muravin.bankapplication.transferService.dto.OperationDto;
import ru.muravin.bankapplication.transferService.service.NotificationsServiceClient;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MainController.class)
public class MainControllerTest {

    @MockitoBean
    private MeterRegistry meterRegistry;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RestTemplate restTemplate;

    @MockitoBean
    private NotificationsServiceClient notificationsServiceClient;



    @Autowired
    private ObjectMapper objectMapper;

    private OperationDto transferDto;

    @BeforeEach
    void setUp() {
        Mockito.when(meterRegistry.counter(any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(mock(Counter.class));
        transferDto = new OperationDto();
        transferDto.setFromAccount("user1");
        transferDto.setToAccount("user2");
        transferDto.setFromCurrency("USD");
        transferDto.setToCurrency("EUR");
        transferDto.setAmount("100");
    }

    @Test
    void transfer_Successful_ReturnsOk() throws Exception {
        AccountInfoDto fromAccount = new AccountInfoDto();
        fromAccount.setAccountNumber("ACC123");
        fromAccount.setBalance(1000f);

        AccountInfoDto toAccount = new AccountInfoDto();
        toAccount.setAccountNumber("ACC456");

        when(restTemplate.getForObject(
                eq("http://gateway/accountsService/getAccountInfo?username=user1&currency=USD"),
                eq(AccountInfoDto.class))).thenReturn(fromAccount);

        when(restTemplate.getForObject(
                eq("http://gateway/accountsService/getAccountInfo?username=user2&currency=EUR"),
                eq(AccountInfoDto.class))).thenReturn(toAccount);

        when(restTemplate.postForObject(eq("http://gateway/antifraudService/checkOperations"), any(OperationDto.class), eq(HttpResponseDto.class)))
                .thenReturn(new HttpResponseDto("OK", ""));

        LinkedHashMap<String, Object> currency1 = LinkedHashMap.newLinkedHashMap(3);
        currency1.put("currencyCode", "EUR");
        currency1.put("buyRate", 85.0);
        currency1.put("sellRate", 85.0);
        LinkedHashMap<String, Object> currency2 = LinkedHashMap.newLinkedHashMap(3);
        currency2.put("currencyCode", "USD");
        currency2.put("sellRate", 75.0);
        currency2.put("buyRate", 75.0);
        when(restTemplate.getForObject(eq("http://gateway/currencyExchangeService/rates"), eq(List.class)))
                .thenReturn(
                        List.of(currency1, currency2)
                );

        when(restTemplate.postForObject(eq("http://gateway/accountsService/transfer"), any(CheckedTransferDto.class), eq(String.class)))
                .thenReturn("OK");

        mockMvc.perform(post("/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("OK"))
                .andExpect(jsonPath("$.statusMessage").value("Перевод успешен"));
    }

    @Test
    void transfer_SameAccountAndCurrency_ReturnsError() throws Exception {
        transferDto.setFromAccount("user1");
        transferDto.setToAccount("user1");
        transferDto.setFromCurrency("RUB");
        transferDto.setToCurrency("RUB");

        mockMvc.perform(post("/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("ERROR"))
                .andExpect(jsonPath("$.statusMessage").value("Счет получателя и счёт отправителя - один и тот же счёт. Перевод отклонён"));
    }

    @Test
    void transfer_FromAccountNotFound_ReturnsError() throws Exception {
        when(restTemplate.getForObject(anyString(), eq(AccountInfoDto.class))).thenReturn(null);

        mockMvc.perform(post("/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("ERROR"))
                .andExpect(jsonPath("$.statusMessage").value("У пользователя user1 не открыт счет в выбранной валюте"));
    }

    @Test
    void transfer_AntifraudBlocks_ReturnsError() throws Exception {
        AccountInfoDto fromAccount = new AccountInfoDto();
        fromAccount.setAccountNumber("ACC123");
        fromAccount.setBalance(1000f);

        AccountInfoDto toAccount = new AccountInfoDto();
        toAccount.setAccountNumber("ACC456");

        when(restTemplate.getForObject(
                eq("http://gateway/accountsService/getAccountInfo?username=user1&currency=USD"),
                eq(AccountInfoDto.class))).thenReturn(fromAccount);

        when(restTemplate.getForObject(
                eq("http://gateway/accountsService/getAccountInfo?username=user2&currency=EUR"),
                eq(AccountInfoDto.class))).thenReturn(toAccount);

        when(restTemplate.postForObject(eq("http://gateway/antifraudService/checkOperations"), any(OperationDto.class), eq(HttpResponseDto.class)))
                .thenReturn(new HttpResponseDto("ERROR", "Blocked by antifraud"));

        mockMvc.perform(post("/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("ERROR"))
                .andExpect(jsonPath("$.statusMessage").value("Blocked by antifraud"));
    }

    @Test
    void transfer_InsufficientFunds_ReturnsError() throws Exception {
        AccountInfoDto fromAccount = new AccountInfoDto();
        fromAccount.setAccountNumber("ACC123");
        fromAccount.setBalance(50f); // Меньше, чем нужно списать

        AccountInfoDto toAccount = new AccountInfoDto();
        toAccount.setAccountNumber("ACC456");

        when(restTemplate.getForObject(
                eq("http://gateway/accountsService/getAccountInfo?username=user1&currency=USD"),
                eq(AccountInfoDto.class))).thenReturn(fromAccount);

        when(restTemplate.getForObject(
                eq("http://gateway/accountsService/getAccountInfo?username=user2&currency=EUR"),
                eq(AccountInfoDto.class))).thenReturn(toAccount);

        when(restTemplate.postForObject(eq("http://gateway/antifraudService/checkOperations"), any(OperationDto.class), eq(HttpResponseDto.class)))
                .thenReturn(new HttpResponseDto("OK", ""));


        LinkedHashMap<String, Object> currency1 = LinkedHashMap.newLinkedHashMap(3);
        currency1.put("currencyCode", "EUR");
        currency1.put("buyRate", 85.0);
        currency1.put("sellRate", 85.0);
        LinkedHashMap<String, Object> currency2 = LinkedHashMap.newLinkedHashMap(3);
        currency2.put("currencyCode", "USD");
        currency2.put("sellRate", 75.0);
        currency2.put("buyRate", 75.0);
        when(restTemplate.getForObject(eq("http://gateway/currencyExchangeService/rates"), eq(List.class)))
                .thenReturn(List.of(currency1, currency2));

        mockMvc.perform(post("/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("ERROR"))
                .andExpect(jsonPath("$.statusMessage").value("На счёте недостаточно средств для списания. Не хватает: 50.0 в валюте Доллар США"));
    }

    @Test
    void transfer_AccountsServiceReturnsError_ShouldReturnError() throws Exception {
        AccountInfoDto fromAccount = new AccountInfoDto();
        fromAccount.setAccountNumber("ACC123");
        fromAccount.setBalance(1000f);

        AccountInfoDto toAccount = new AccountInfoDto();
        toAccount.setAccountNumber("ACC456");

        when(restTemplate.getForObject(
                eq("http://gateway/accountsService/getAccountInfo?username=user1&currency=USD"),
                eq(AccountInfoDto.class))).thenReturn(fromAccount);

        when(restTemplate.getForObject(
                eq("http://gateway/accountsService/getAccountInfo?username=user2&currency=EUR"),
                eq(AccountInfoDto.class))).thenReturn(toAccount);

        when(restTemplate.postForObject(eq("http://gateway/antifraudService/checkOperations"), any(OperationDto.class), eq(HttpResponseDto.class)))
                .thenReturn(new HttpResponseDto("OK", ""));


        LinkedHashMap<String, Object> currency1 = LinkedHashMap.newLinkedHashMap(3);
        currency1.put("currencyCode", "EUR");
        currency1.put("buyRate", 85.0);
        currency1.put("sellRate", 85.0);
        LinkedHashMap<String, Object> currency2 = LinkedHashMap.newLinkedHashMap(3);
        currency2.put("currencyCode", "USD");
        currency2.put("sellRate", 75.0);
        currency2.put("buyRate", 75.0);
        when(restTemplate.getForObject(eq("http://gateway/currencyExchangeService/rates"), eq(List.class)))
                .thenReturn(List.of(currency1, currency2));

        when(restTemplate.postForObject(eq("http://gateway/accountsService/transfer"), any(CheckedTransferDto.class), eq(String.class)))
                .thenReturn("Ошибка перевода");

        mockMvc.perform(post("/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("ERROR"))
                .andExpect(jsonPath("$.statusMessage").value("Ошибка перевода"));
    }
}