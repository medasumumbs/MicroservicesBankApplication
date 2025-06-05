package ru.muravin.bankapplication.uiService.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.muravin.bankapplication.uiService.dto.*;

import java.util.List;

@Service
public class AccountsService {

    private final WebClient.Builder webClientBuilder;

    @Value("${gatewayHost:gateway}")
    private String gatewayHost;

    @Autowired
    public AccountsService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public Mono<String> addAccount(String login, String currency) {
        var dto = new NewAccountDto(currency, login);
        return webClientBuilder.build()
            .post()
            .uri("http://"+gatewayHost+"/accountsService/addAccount")
            .bodyValue(dto).retrieve()
            .bodyToMono(String.class)
            .onErrorResume(
                    WebClientResponseException.class,
                    ex -> {
                        if (ex.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                            return Mono.just("Внутренняя ошибка сервера: " + ex.getMessage());
                        } else {
                            return Mono.error(ex);
                        }
                    }
            )
            .onErrorResume(Exception.class, ex -> Mono.just("Возникла неизвестная ошибка: " + ex.getMessage()));
    }
    public Mono<List<AccountDto>> findAllAccountsByUser(String login) {
        return webClientBuilder.build().get().uri("http://"+gatewayHost+"/accountsService/findAccountsByUsername?username="+login)
                .retrieve().bodyToFlux(AccountDto.class).collectList().switchIfEmpty(Mono.empty());
    }
    public Mono<HttpResponseDto> withdrawCash(String login, String currency, String amount, String action) {
        var dto = new CashInCashOutDto();
        dto.setCurrencyCode(currency);
        dto.setLogin(login);
        dto.setAction(action);
        dto.setAmount(amount);
        return webClientBuilder
            .build()
            .post()
            .uri("http://"+gatewayHost+"/cashInCashOutService/withdrawCash")
                .bodyValue(dto)
            .retrieve().bodyToMono(HttpResponseDto.class)
            .onErrorResume(
                    WebClientResponseException.class,
                    ex -> {
                        if (ex.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                            return Mono.just(new HttpResponseDto("ERROR","Внутренняя ошибка сервера: " + ex.getMessage()));
                        } else {
                            return Mono.error(ex);
                        }
                    }
            )
            .onErrorResume(Exception.class, ex -> Mono.just(new HttpResponseDto("ERROR","Возникла неизвестная ошибка: " + ex.getMessage())));
    }

    public Mono<HttpResponseDto> transfer(String fromCurrency, String toCurrency, String fromLogin, String toLogin, String value) {
        var dto = new TransferDto();
        dto.setAmount(value);
        dto.setFromAccount(fromLogin);
        dto.setToAccount(toLogin);
        dto.setFromCurrency(fromCurrency);
        dto.setToCurrency(toCurrency);
        return webClientBuilder
            .build()
            .post()
            .uri("http://"+gatewayHost+"/transferService/transfer")
            .bodyValue(dto)
            .retrieve().bodyToMono(HttpResponseDto.class)
            .onErrorResume(
                    WebClientResponseException.class,
                    ex -> {
                        if (ex.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                            return Mono.just(new HttpResponseDto("ERROR","Внутренняя ошибка сервера: " + ex.getMessage()));
                        } else {
                            return Mono.error(ex);
                        }
                    }
            )
            .onErrorResume(Exception.class, ex -> Mono.just(new HttpResponseDto("ERROR","Возникла неизвестная ошибка: " + ex.getMessage())));
    }

    public Flux<UserDto> findAllUsers() {
        return webClientBuilder
            .build()
            .get()
            .uri("http://"+gatewayHost+"/accountsService/users")
            .retrieve().bodyToFlux(UserDto.class)
            .onErrorResume(
                    WebClientResponseException.class,
                    ex -> {
                        if (ex.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                            System.out.println(ex.getMessage() + ": " + ex.getResponseBodyAsString());
                            return Flux.empty();
                        } else {
                            return Flux.error(ex);
                        }
                    }
            )
            .onErrorResume(Exception.class, ex -> {
                System.out.println(ex.getMessage());
                return Flux.empty();
            });
    }
}
