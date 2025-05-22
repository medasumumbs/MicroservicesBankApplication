package ru.muravin.bankapplication.accountsService;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.muravin.bankapplication.accountsService.configuration.OAuth2SecurityConfig;
import ru.muravin.bankapplication.accountsService.controller.MainController;
import ru.muravin.bankapplication.accountsService.model.Account;
import ru.muravin.bankapplication.accountsService.model.User;
import ru.muravin.bankapplication.accountsService.repository.AccountsRepository;
import ru.muravin.bankapplication.accountsService.repository.UsersRepository;
import ru.muravin.bankapplication.accountsService.service.NotificationsServiceClient;


@SpringBootTest(
        classes = {TestApplicationConfiguration.class, AccountsServiceApplication.class},
        properties = {
                "spring.datasource.url=jdbc:h2:mem:testdb",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
                "spring.jpa.hibernate.ddl-auto=update",
                "spring.liquibase.enabled=false",
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

    @Autowired
    public UsersRepository usersRepository;

    @Autowired
    public AccountsRepository accountsRepository;

    @MockitoBean
    public NotificationsServiceClient notificationsServiceClient;

    @BeforeEach
    void setup() {
        usersRepository.deleteAll();
        usersRepository.save(User.builder().login("john_doe").build());
        accountsRepository.save(Account.builder().Balance(500f).currency("RUB").build());
        // Устанавливаем MockMvc для RestAssuredMockMvc
        // Настраиваем RestAssuredMockMvc с нужным контекстом
        io.restassured.module.mockmvc.RestAssuredMockMvc.mockMvc(
                MockMvcBuilders.webAppContextSetup(context).build()
        );
    }
    @Autowired
    private ObjectMapper objectMapper; // <-- автоматически доступен в Spring Boot
}