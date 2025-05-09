package ru.muravin.bankapplication.uiService.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import ru.muravin.bankapplication.uiService.dto.ChangePasswordDto;
import ru.muravin.bankapplication.uiService.dto.UserDto;

import java.util.HashMap;

@Service
public class ReactiveUserDetailsServiceImpl implements ReactiveUserDetailsService {

    private final WebClient.Builder webClientBuilder;
    private final PasswordEncoder passwordEncoder;

    public ReactiveUserDetailsServiceImpl(WebClient.Builder webClientBuilder, PasswordEncoder passwordEncoder) {
        this.webClientBuilder = webClientBuilder;
        this.passwordEncoder = passwordEncoder;
    }

    public Mono<String> updatePassword(String login, String password) {
        return webClientBuilder.build()
            .post().uri("http://gateway/accountsService/changePassword")
            .bodyValue(ChangePasswordDto.builder().newPassword(passwordEncoder.encode(password)).login(login).build())
                .retrieve().bodyToMono(String.class)
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

    public Mono<String> registerUser(UserDto userDto) {
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
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
        var url = "http://gateway/accountsService/findByUsername";
        return webClientBuilder.build()
            .get().uri(UriComponentsBuilder.fromHttpUrl(url).queryParam("username", username).build().toUri())
            .retrieve().bodyToMono(UserDto.class).map(userDto -> (UserDetails) userDto).onErrorResume(
                ex -> {
                    System.out.println(ex.getMessage());
                    return Mono.error(ex);
                }
            ).onErrorResume(
                    e -> Mono.empty()
            );
    }

    public Mono<UserDto> getCurrentUser() {
        return ReactiveSecurityContextHolder.getContext().map(securityContext -> {
            var user = securityContext.getAuthentication().getPrincipal();
            if (!(user instanceof UserDto)) {
                return UserDto.builder().build();
            }
            return ((UserDto) user);
        }).defaultIfEmpty(UserDto.builder().build());
    }

    public Mono<String> updateUserInfo(String login, String birthDate, String name) {
        var userDto = UserDto.builder().build();
        userDto.setLogin(login);
        userDto.setDateOfBirth(birthDate);
        userDto.setFirstName(name.split(" ")[1]);
        userDto.setLastName(name.split(" ")[0]);
        if (name.split(" ").length > 2) userDto.setPatronymic(name.split(" ")[2]);
        return webClientBuilder.build()
                .post().uri("http://gateway/accountsService/updateUserInfo")
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
}
