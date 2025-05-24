package ru.muravin.bankapplication.uiService.controllerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import java.time.LocalDate;
import java.util.List;

import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.muravin.bankapplication.uiService.TestSecurityConfig;
import ru.muravin.bankapplication.uiService.configurations.OAuth2SecurityConfig;
import ru.muravin.bankapplication.uiService.controller.MainController;
import ru.muravin.bankapplication.uiService.dto.CurrencyDto;
import ru.muravin.bankapplication.uiService.dto.UserDto;
import ru.muravin.bankapplication.uiService.service.AccountsService;
import ru.muravin.bankapplication.uiService.service.CurrenciesService;
import ru.muravin.bankapplication.uiService.service.ReactiveUserDetailsServiceImpl;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = MainController.class)
@TestPropertySource(locations = "classpath:application.yml")
@Import(TestSecurityConfig.class)
public class MainControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ReactiveUserDetailsServiceImpl reactiveUserDetailsServiceImpl;

    @MockitoBean
    private CurrenciesService currenciesService;

    @MockitoBean
    private AccountsService accountsService;

    private final String login = "john_doe";
    private final UserDto userDto = UserDto.builder().login(login)
        .firstName("John").lastName("Doe").patronymic("Smith")
            .dateOfBirth(LocalDate.of(1990, 1, 1).toString()).build();

    @BeforeEach
    void setupWithCsrf() {
        this.webTestClient = webTestClient.mutate()
                .apply(csrf())
                .build();
    }

    @Test
    void testMainPage_ReturnsViewWithModelAttributes() {
        // Given
        CsrfToken csrfToken = mock(CsrfToken.class);
        when(csrfToken.getToken()).thenReturn("csrf-token");

        CurrencyDto currencyDto = CurrencyDto.builder().currencyCode("RUB").title("Рубль").build();
        doReturn(Flux.just(currencyDto)).when(currenciesService).getCurrenciesList();
        when(accountsService.findAllAccountsByUser(login)).thenReturn(Mono.just(List.of()));
        doReturn(Flux.just(userDto)).when(accountsService).findAllUsers();
        when(reactiveUserDetailsServiceImpl.getCurrentUser()).thenReturn(Mono.just(userDto));

        // When & Then
        webTestClient.get().uri("/main")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(response -> {
                    // Проверяем, что страница загружена
                });
    }

    @Test
    void testChangeUserInfo_InvalidName_ShowsError() {
        // Given
        CsrfToken csrfToken = mock(CsrfToken.class);
        when(csrfToken.getToken()).thenReturn("csrf-token");

        when(reactiveUserDetailsServiceImpl.getCurrentUser()).thenReturn(Mono.just(userDto));
        when(accountsService.findAllAccountsByUser(login)).thenReturn(Mono.just(List.of()));
        doReturn(Flux.just(userDto)).when(accountsService).findAllUsers();

        // When & Then
        webTestClient.post().uri("/user/john_doe/changeInfo")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("name", "Ivanov")
                        .with("birthDate", "1990-01-01"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(response -> {
                    String responseBody = new String(response.getResponseBody());
                    assertTrue(responseBody.contains("ФИО должно содержать как минимум фамилию и имя"));
                });
    }

    @Test
    void testChangeUserInfo_InvalidBirthDate_ShowsError() {
        // Given
        CsrfToken csrfToken = mock(CsrfToken.class);
        when(csrfToken.getToken()).thenReturn("csrf-token");

        when(reactiveUserDetailsServiceImpl.getCurrentUser()).thenReturn(Mono.just(userDto));
        when(accountsService.findAllAccountsByUser(login)).thenReturn(Mono.just(List.of()));
        doReturn(Flux.just(userDto)).when(accountsService).findAllUsers();

        // When & Then
        webTestClient.post().uri("/user/john_doe/changeInfo")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("name", "Ivanov Ivan Ivanovich")
                        .with("birthDate", "invalid-date"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(response -> {
                    String responseBody = new String(response.getResponseBody());
                    assertTrue(responseBody.contains("Использован некорректный формат даты. Корректный формат - yyyy-MM-dd"));
                });
    }

    @Test
    void testChangeUserInfo_UserUnderage_ShowsError() {
        // Given
        CsrfToken csrfToken = mock(CsrfToken.class);
        when(csrfToken.getToken()).thenReturn("csrf-token");

        when(reactiveUserDetailsServiceImpl.getCurrentUser()).thenReturn(Mono.just(userDto));
        when(accountsService.findAllAccountsByUser(login)).thenReturn(Mono.just(List.of()));
        doReturn(Flux.just(userDto)).when(accountsService).findAllUsers();
        when(reactiveUserDetailsServiceImpl.updateUserInfo(anyString(), anyString(), anyString()))
                .thenAnswer(invocation -> Mono.just("Пользователь не может быть несовершеннолетним"));

        // When & Then
        webTestClient.post().uri("/user/john_doe/changeInfo")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("name", "Ivanov Ivan Ivanovich")
                        .with("birthDate", "2010-01-01"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(response -> {
                    String responseBody = new String(response.getResponseBody());
                    assertTrue(responseBody.contains("Пользователь не может быть несовершеннолетним"));
                });
    }
}