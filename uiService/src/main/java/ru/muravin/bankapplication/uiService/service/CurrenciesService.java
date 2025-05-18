package ru.muravin.bankapplication.uiService.service;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.muravin.bankapplication.uiService.dto.CurrencyDto;
import ru.muravin.bankapplication.uiService.dto.CurrencyRateDto;
import ru.muravin.bankapplication.uiService.dto.UserDto;

import java.util.HashMap;
import java.util.List;

@Service
public class CurrenciesService {
    private final WebClient.Builder webClientBuilder;

    public CurrenciesService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public Mono<ResponseEntity<byte[]>> getRates() {
       return webClientBuilder.build().get().uri("http://gateway/currencyExchangeService/rates").retrieve()
                .bodyToMono(String.class).map(String::getBytes).map(ResponseEntity::ok);
    }

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
    public Flux<CurrencyDto> getCurrenciesList() {
        var titlesByCodesMap = new HashMap<String, String >();
        titlesByCodesMap.put("USD", "Доллар США");
        titlesByCodesMap.put("RUB", "Российский рубль");
        titlesByCodesMap.put("EUR", "Евро");
        return webClientBuilder
                .build().get().uri("http://gateway/currencyExchangeService/rates").retrieve()
                .bodyToFlux(CurrencyRateDto.class).switchIfEmpty(Flux.empty()).map(currencyRateDto -> {
                    return CurrencyDto.builder()
                            .currencyCode(currencyRateDto.getCurrencyCode())
                            .title(titlesByCodesMap.get(currencyRateDto.getCurrencyCode()))
                            .build();
                });
    }
}
