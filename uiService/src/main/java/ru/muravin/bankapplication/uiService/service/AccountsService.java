package ru.muravin.bankapplication.uiService.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.muravin.bankapplication.uiService.dto.NewAccountDto;

@Service
public class AccountsService {

    private final WebClient.Builder webClientBuilder;

    @Autowired
    public AccountsService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public Mono<String> addAccount(String login, String currency) {
        var dto = new NewAccountDto(currency, login);
        return webClientBuilder.build()
            .post()
            .uri("http://gateway/accountsService/addAccount")
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
}
