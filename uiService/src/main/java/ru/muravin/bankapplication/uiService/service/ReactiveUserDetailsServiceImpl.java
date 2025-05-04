package ru.muravin.bankapplication.uiService.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.muravin.bankapplication.uiService.dto.UserDto;

@Service
public class ReactiveUserDetailsServiceImpl implements ReactiveUserDetailsService {

    private final WebClient.Builder webClientBuilder;

    public ReactiveUserDetailsServiceImpl(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public Mono<String> registerUser(UserDto userDto) {
        return webClientBuilder.build()
        .post().uri("http://gateway/accountsService/register")
        .bodyValue(userDto).retrieve().bodyToMono(String.class).onErrorResume(
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

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        //Todo сделать rest запрос в сервис accountsService
        return null;
    }
}
