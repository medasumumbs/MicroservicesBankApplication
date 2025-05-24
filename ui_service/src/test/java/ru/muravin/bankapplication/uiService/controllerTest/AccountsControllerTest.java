package ru.muravin.bankapplication.uiService.controllerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDate;
import java.util.List;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.muravin.bankapplication.uiService.TestSecurityConfig;
import ru.muravin.bankapplication.uiService.controller.AccountsController;
import ru.muravin.bankapplication.uiService.controller.MainController;
import ru.muravin.bankapplication.uiService.dto.CurrencyDto;
import ru.muravin.bankapplication.uiService.dto.HttpResponseDto;
import ru.muravin.bankapplication.uiService.dto.UserDto;
import ru.muravin.bankapplication.uiService.service.AccountsService;
import ru.muravin.bankapplication.uiService.service.CurrenciesService;
import ru.muravin.bankapplication.uiService.service.ReactiveUserDetailsServiceImpl;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.web.reactive.function.BodyInserters.fromFormData;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = AccountsController.class)
@TestPropertySource(locations = "classpath:application.yml")
@Import(TestSecurityConfig.class)
public class AccountsControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private AccountsService accountsService;

    @MockitoBean
    private ReactiveUserDetailsServiceImpl reactiveUserDetailsServiceImpl;

    @MockitoBean
    private CurrenciesService currenciesService;

    private final String login = "john_doe";
    private final UserDto userDto = UserDto.builder()
            .login(login)
            .firstName("John")
            .lastName("Doe")
            .patronymic("Smith")
            .dateOfBirth(LocalDate.now().toString())
            .build();

    @BeforeEach
    void setupWithCsrf() {
        this.webTestClient = webTestClient.mutate()
                .apply(SecurityMockServerConfigurers.csrf())
                .build();
    }
    @Test
    void proxyCurrencyExchangeGetRates_returnsOk() {
        CsrfToken csrfToken = mock(CsrfToken.class);
        when(csrfToken.getToken()).thenReturn("csrf-token");
        // Given
        when(currenciesService.getRates()).thenReturn(Mono.just(ResponseEntity.ok().body("OK".getBytes())));

        // When & Then
        webTestClient.get().uri("/currencyExchangeService/rates")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void changePassword_validData_redirectsToMain() {
        // Given
        doReturn(Flux.just(CurrencyDto.builder().build())).when(currenciesService).getCurrenciesList();
        CsrfToken csrfToken = mock(CsrfToken.class);
        when(csrfToken.getToken()).thenReturn("csrf-token");

        when(accountsService.findAllAccountsByUser(anyString())).thenReturn(Mono.just(List.of()));
        when(reactiveUserDetailsServiceImpl.getCurrentUser()).thenReturn(Mono.just(userDto));
        when(accountsService.addAccount(anyString(), anyString())).thenReturn(Mono.just("OK"));

        // When & Then
        webTestClient.post().uri("/user/john_doe/accounts")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(fromFormData("currency", "RUB"))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/main");
    }

    @Test
    void withdrawCash_validData_redirectsToMain() {
        CsrfToken csrfToken = mock(CsrfToken.class);
        when(csrfToken.getToken()).thenReturn("csrf-token");
        // Given
        when(reactiveUserDetailsServiceImpl.getCurrentUser()).thenReturn(Mono.just(userDto));
        doReturn(Flux.just(CurrencyDto.builder().build())).when(currenciesService).getCurrenciesList();
        when(accountsService.findAllAccountsByUser(anyString())).thenReturn(Mono.just(List.of()));
        when(accountsService.withdrawCash(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.just(new HttpResponseDto("OK", "Success")));

        // When & Then
        webTestClient.post().uri("/user/john_doe/cash")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(fromFormData("currency", "USD").with("value", "100").with("action", "PUT"))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/main");
    }

    @Test
    void transfer_validData_redirectsToMain() {
        CsrfToken csrfToken = mock(CsrfToken.class);
        when(csrfToken.getToken()).thenReturn("csrf-token");
        // Given
        when(reactiveUserDetailsServiceImpl.getCurrentUser()).thenReturn(Mono.just(userDto));
        doReturn(Flux.just(CurrencyDto.builder().build())).when(currenciesService).getCurrenciesList();
        doReturn(Mono.just(List.of())).when(accountsService).findAllAccountsByUser(anyString());
        when(accountsService.transfer(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.just(new HttpResponseDto("OK", "Transfer successful")));

        // When & Then
        webTestClient.post().uri("/user/john_doe/transfer")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(fromFormData("from_currency", "USD")
                        .with("to_currency", "EUR")
                        .with("to_login", "jane_doe")
                        .with("value", "100"))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/main");
    }
}