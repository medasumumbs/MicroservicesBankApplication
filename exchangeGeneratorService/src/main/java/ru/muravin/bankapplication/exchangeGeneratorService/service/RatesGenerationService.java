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

@Service
public class RatesGenerationService {

    private final WebClient.Builder webClientBuilder;

    public RatesGenerationService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public Float createRandomFloat() {
        // Генерируем случайное число типа double в диапазоне [100.00, 150.00] с двумя знаками после запятой
        // Преобразуем в float
        Random random = new Random(System.currentTimeMillis());
        return (float) (Math.round((100.0 + random.nextDouble() * 50.0) * 100) / 100.0);
    }

    @Scheduled(fixedDelay = 1000)
    public Mono<HttpResponseDto> generateAndSendRates() {
        var rates = new ArrayList<CurrencyRateDto>();
        CurrencyRateDto currencyRateDto = new CurrencyRateDto();
        currencyRateDto.setBuyRate(createRandomFloat());
        currencyRateDto.setSellRate(createRandomFloat());
        currencyRateDto.setCurrencyCode("USD");
        rates.add(currencyRateDto);
        CurrencyRateDto currencyRateDto2 = new CurrencyRateDto();
        currencyRateDto2.setBuyRate(createRandomFloat());
        currencyRateDto2.setSellRate(createRandomFloat());
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

}
