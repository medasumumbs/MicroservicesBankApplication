package ru.muravin.bankapplication.exchangeGeneratorService.service;

import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.muravin.bankapplication.exchangeGeneratorService.dto.CurrencyRateDto;
import ru.muravin.bankapplication.exchangeGeneratorService.dto.HttpResponseDto;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class RatesGenerationService {

    private final WebClient.Builder webClientBuilder;

    public RatesGenerationService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public Mono<Float> createRandomFloat() {
        var currentTimeMillisLast5Digits = System.currentTimeMillis() % 100000;
        return Mono.just((float) (currentTimeMillisLast5Digits / 100.0));
    }

    @Scheduled(fixedRate = 1000)
    public void generateAndSendRates() {
        createRandomFloat().flatMap(
            randomFloat -> {
                var rates = new ArrayList<CurrencyRateDto>();
                CurrencyRateDto currencyRateDto = new CurrencyRateDto();
                currencyRateDto.setBuyRate(randomFloat);
                currencyRateDto.setSellRate(randomFloat - 3);
                currencyRateDto.setCurrencyCode("USD");
                rates.add(currencyRateDto);
                CurrencyRateDto currencyRateDto2 = new CurrencyRateDto();
                currencyRateDto2.setBuyRate(randomFloat - 10);
                currencyRateDto2.setSellRate(randomFloat - 15);
                currencyRateDto2.setCurrencyCode("EUR");
                rates.add(currencyRateDto2);
                CurrencyRateDto currencyRateDto3 = new CurrencyRateDto();
                currencyRateDto3.setBuyRate(1f);
                currencyRateDto3.setSellRate(1f);
                currencyRateDto3.setCurrencyCode("RUB");
                rates.add(currencyRateDto3);
                return webClientBuilder.build()
                    .post().uri("http://gateway/currencyExchangeService/rates")
                    .bodyValue(rates).retrieve().bodyToMono(HttpResponseDto.class).onErrorResume(
                            WebClientResponseException.class,
                            ex -> {
                                if (ex.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                                    return Mono.just(new HttpResponseDto("500", "Internal Server Error"));
                                } else {
                                    return Mono.error(ex);
                                }
                            }
                    )
                    .onErrorResume(Exception.class, ex -> Mono.just(
                            new HttpResponseDto("500","Возникла неизвестная ошибка: " + ex.getMessage()))
                    );
            }
        ).block();
    }

}
