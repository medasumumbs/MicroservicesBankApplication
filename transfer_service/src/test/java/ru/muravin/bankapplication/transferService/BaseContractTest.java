/*

package ru.muravin.bankapplication.cashInCashOutService;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import ru.muravin.bankapplication.cashInCashOutService.configuration.OAuth2SecurityConfig;
import ru.muravin.bankapplication.cashInCashOutService.dto.HttpResponseDto;
import ru.muravin.bankapplication.cashInCashOutService.service.NotificationsServiceClient;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = {CashInCashOutServiceApplication.class},
        properties = {
                "spring.autoconfigure.exclude=" +
                        "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration",
                "spring.security.enable-method-security=false"
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


    @MockitoBean
    protected RestTemplate restTemplate;

    @MockitoBean
    protected NotificationsServiceClient notificationsServiceClient;

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
                        Mockito.eq("http://gateway/antifraudService/checkOperations"), Mockito.any(), Mockito.any()))
                .thenReturn(new HttpResponseDto("OK", ""));

        Mockito.when(restTemplate.postForObject(
                        Mockito.eq("http://gateway/accountsService/cashInOrCashOut"), Mockito.any(), Mockito.any()))
                .thenReturn("OK");
    }

}
*/
