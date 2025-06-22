package ru.muravin.bankapplication.transferService;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import ru.muravin.bankapplication.transferService.TransferServiceApplication;
import ru.muravin.bankapplication.transferService.configuration.OAuth2SecurityConfig;
import ru.muravin.bankapplication.transferService.dto.AccountInfoDto;
import ru.muravin.bankapplication.transferService.dto.CurrencyDto;
import ru.muravin.bankapplication.transferService.dto.HttpResponseDto;
import ru.muravin.bankapplication.transferService.service.NotificationsServiceClient;


import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = {TransferServiceApplication.class},
        properties = {
                "spring.autoconfigure.exclude=" +
                        "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration",
                "spring.security.enable-method-security=false",
                "management.metrics.enabled=false",
                "spring.boot.actuate.metrics.enabled=false",
                "metricsEnabled=false"
        }
)
@AutoConfigureMessageVerifier
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class BaseContractTest {

    @MockitoBean
    private OAuth2SecurityConfig oAuth2SecurityConfig;

    @MockitoBean
    private JwtAuthenticationConverter jwtAuthenticationConverter;

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MeterRegistry meterRegistry;

    @MockitoBean
    protected RestTemplate restTemplate;

    @MockitoBean
    protected ru.muravin.bankapplication.transferService.service.NotificationsServiceClient notificationsServiceClient;

    @Autowired
    protected ObjectMapper objectMapper;


    @BeforeEach
    void setup() {
        // Устанавливаем MockMvc для RestAssuredMockMvc
        // Настраиваем RestAssuredMockMvc с нужным контекстом
        io.restassured.module.mockmvc.RestAssuredMockMvc.mockMvc(
                MockMvcBuilders.webAppContextSetup(context).build()
        );
        // Мокаем успешный ответ от antifraud и accountsService
        Mockito.when(restTemplate.postForObject(
                        eq("http://gateway/antifraudService/checkOperations"), Mockito.any(), Mockito.any()))
                .thenReturn(new HttpResponseDto("OK", ""));

        Mockito.when(restTemplate.postForObject(
                        eq("http://gateway/accountsService/transfer"), Mockito.any(), Mockito.any()))
                .thenReturn("OK");

        var accountInfoDto = new AccountInfoDto();
        accountInfoDto.setAccountNumber("123456789");
        accountInfoDto.setCurrency(CurrencyDto.builder().currencyCode("RUB").build());
        accountInfoDto.setBalance(500f);
        Mockito.when(restTemplate.getForObject(
                eq("http://gateway/accountsService/getAccountInfo?username=john_doe&currency=RUB"),
                any())
        ).thenReturn(accountInfoDto);

        var accountInfoDto2 = new AccountInfoDto();
        accountInfoDto2.setAccountNumber("123456781");
        accountInfoDto2.setCurrency(CurrencyDto.builder().currencyCode("RUB").build());
        accountInfoDto2.setBalance(500f);
        Mockito.when(restTemplate.getForObject(
                eq("http://gateway/accountsService/getAccountInfo?username=jane_doe&currency=RUB"),
                any())
        ).thenReturn(accountInfoDto2);

    }

}

