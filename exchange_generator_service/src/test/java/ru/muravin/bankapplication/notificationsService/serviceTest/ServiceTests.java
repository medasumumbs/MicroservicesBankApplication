/*
package ru.muravin.bankapplication.notificationsService.serviceTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.muravin.bankapplication.exchangeGeneratorService.dto.CurrencyRateDto;
import ru.muravin.bankapplication.exchangeGeneratorService.dto.HttpResponseDto;
import ru.muravin.bankapplication.exchangeGeneratorService.service.RatesGenerationService;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ServiceTests {

    private WebClient.Builder webClientBuilder;
    private WebClient mockWebClient;
    private RatesGenerationService ratesGenerationService;

    @BeforeEach
    void setUp() {
        webClientBuilder = mock(WebClient.Builder.class);
        mockWebClient = mock(WebClient.class);

        when(webClientBuilder.build()).thenReturn(mockWebClient);
        ratesGenerationService = new RatesGenerationService(webClientBuilder);
        ratesGenerationService.setGatewayHost("gateway");
    }

    @Test
    void testCreateRandomFloat_shouldReturnValidValue() {
        Mono<Float> result = RatesGenerationService.createRandomFloat();
        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();
        Float value = result.block(Duration.ofSeconds(5));
        assert value != null;
        assert value >= 0;
    }

    @Test
    void testGenerateAndSendRates_shouldSendTwoCurrencyRates() {
        var randomFloat = 12345f;

        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClientBuilder.build()).thenReturn(mockWebClient);
        when(mockWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(HttpResponseDto.class)).thenReturn(Mono.just(new HttpResponseDto("200", "OK")));

        try (var mocked = Mockito.mockStatic(RatesGenerationService.class)) {
            mocked.when(() -> RatesGenerationService.createRandomFloat()).thenReturn(Mono.just(randomFloat));

            ratesGenerationService.generateAndSendRates();
            verify(mockWebClient, times(1)).post();
            verify(requestBodyUriSpec, times(1)).uri("http://gateway/currencyExchangeService/rates");
            verify(requestHeadersSpec, times(1)).retrieve();
            verify(responseSpec, times(1)).bodyToMono(HttpResponseDto.class);
        }
    }

    @Test
    void testGenerateAndSendRates_shouldHandleServerError() {
        var randomFloat = 12345f;
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(webClientBuilder.build()).thenReturn(mockWebClient);
        when(mockWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(HttpResponseDto.class)).thenReturn(
                Mono.error(new WebClientResponseException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error", null, null, null)));
        try (var mocked = Mockito.mockStatic(RatesGenerationService.class)) {
            mocked.when(() -> RatesGenerationService.createRandomFloat()).thenReturn(Mono.just(randomFloat));
            ratesGenerationService.generateAndSendRates();
            verify(responseSpec, times(1)).bodyToMono(HttpResponseDto.class);
        }
    }

    @Test
    void testGenerateAndSendRates_shouldHandleUnknownError() {
        var randomFloat = 12345f;

        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClientBuilder.build()).thenReturn(mockWebClient);
        when(mockWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(HttpResponseDto.class)).thenReturn(
                Mono.error(new RuntimeException("Unknown error")));
        try (var mocked = Mockito.mockStatic(RatesGenerationService.class)) {
            mocked.when(() -> RatesGenerationService.createRandomFloat()).thenReturn(Mono.just(randomFloat));
            ratesGenerationService.generateAndSendRates();
            verify(responseSpec, times(1)).bodyToMono(HttpResponseDto.class);
        }
    }
}*/
