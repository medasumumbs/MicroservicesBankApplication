package ru.muravin.bankapplication.uiService.controllerTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;
import ru.muravin.bankapplication.uiService.TestSecurityConfig;
import ru.muravin.bankapplication.uiService.controller.AuthenticationController;
import ru.muravin.bankapplication.uiService.service.ReactiveUserDetailsServiceImpl;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.web.reactive.function.BodyInserters.fromFormData;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = AuthenticationController.class)
@TestPropertySource(locations = "classpath:application.yml")
@Import(TestSecurityConfig.class)
public class AuthenticationControllerTest {


    @BeforeEach
    void setupWithCsrf() {
        this.webTestClient = webTestClient.mutate()
                .apply(SecurityMockServerConfigurers.csrf())
                .build();
    }
    
    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ReactiveUserDetailsServiceImpl reactiveUserDetailsServiceImpl;

    private final String login = "john_doe";
    private final String password = "password123";
    private final String name = "Doe John Smith";
    private final String dateOfBirth = LocalDate.of(1990, 1, 1).toString();

    @Test
    void signUpPage_returnsSignupView() {
        // When & Then
        webTestClient.get().uri("/signup")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(response -> {
                    String responseBody = new String(response.getResponseBody());
                    assertTrue(responseBody.contains("name=\"_csrf\"")); // Проверяем, что CSRF токен добавлен
                });
    }

    @Test
    void registerUser_validData_redirectsToMain() {
        // Given
        when(reactiveUserDetailsServiceImpl.registerUser(any()))
                .thenReturn(Mono.just("OK"));

        var formData = new HashMap<String, String>();
        formData.put("login", login);
        formData.put("password", password);
        formData.put("confirm_password", password);
        formData.put("name", name);
        formData.put("dateOfBirth", dateOfBirth);
        MultiValueMap<String, String> formDataMap = MultiValueMap.fromSingleValue(formData);


        // When & Then
        webTestClient.post().uri("/signup")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(fromFormData(formDataMap))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/main?registeredNewUser=true");
    }

    @Test
    void changePassword_validData_redirectsToMainWithChangedPasswordFlag() {
        // Given
        when(reactiveUserDetailsServiceImpl.updatePassword(anyString(), anyString()))
                .thenReturn(Mono.just("OK"));


        var formData = new HashMap<String, String>();
        formData.put("password", "newPassword123");
        formData.put("confirm_password", "newPassword123");
        MultiValueMap<String, String> formDataMap = MultiValueMap.fromSingleValue(formData);

        // When & Then
        webTestClient.post().uri("/user/john_doe/editPassword")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(fromFormData(formDataMap))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/main?changedPassword=true");
    }
}