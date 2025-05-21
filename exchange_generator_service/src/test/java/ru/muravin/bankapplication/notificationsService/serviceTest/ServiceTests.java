package ru.muravin.bankapplication.notificationsService.serviceTest;

import org.junit.jupiter.api.BeforeEach;
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
    }

    @Test
    void testCreateRandomFloat_shouldReturnValidValue() {
        // Act
        Mono<Float> result = RatesGenerationService.createRandomFloat();

        // Assert
        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();
        Float value = result.block(Duration.ofSeconds(5));
        assert value != null;
        assert value >= 0;
    }

    @Test
    void testGenerateAndSendRates_shouldSendTwoCurrencyRates() {
        // Arrange
        var randomFloat = 12345f;

        // Mock WebClient chain: post -> uri -> body -> retrieve
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestHeadersSpec<?> requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClientBuilder.build()).thenReturn(mockWebClient);
        when(mockWebClient.post()).thenReturn(requestBodyUriSpec);
        ///when(requestBodyUriSpec.uri(any(URI.class))).thenReturn()


        when(mockWebClient.post()).thenReturn((WebClient.RequestBodyUriSpec) requestBodySpec);
        when(requestBodySpec.uri("http://gateway/currencyExchangeService/rates")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.body(any())).thenReturn(requestHeadersSpec); // <-- body используется после uri
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(HttpResponseDto.class)).thenReturn(Mono.just(new HttpResponseDto("200", "OK")));

        try (var mocked = Mockito.mockStatic(RatesGenerationService.class)) {
            mocked.when(() -> RatesGenerationService.createRandomFloat()).thenReturn(Mono.just(randomFloat));

            // Act
            ratesGenerationService.generateAndSendRates();

            // Assert
            verify(mockWebClient, times(1)).post();
            verify(requestBodySpec, times(1)).uri("http://gateway/currencyExchangeService/rates");
            verify(requestHeadersSpec, times(1)).body(any());
            verify(responseSpec, times(1)).bodyToMono(HttpResponseDto.class);
        }
    }

    @Test
    void testGenerateAndSendRates_shouldHandleServerError() {
        // Arrange
        var randomFloat = 12345f;

        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec<?> requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(mockWebClient.post()).thenReturn(requestBodySpec);
        when(requestBodySpec.uri("http://gateway/currencyExchangeService/rates")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(HttpResponseDto.class))
                .thenReturn(Mono.error(new WebClientResponseException(HttpStatus.INTERNAL_SERVER_ERROR, "Error", null, null, null)));

        // Replace createRandomFloat to return fixed value
        try (var mocked = Mockito.mockStatic(RatesGenerationService.class)) {
            mocked.when(() -> RatesGenerationService.createRandomFloat()).thenReturn(Mono.just(randomFloat));

            // Act
            ratesGenerationService.generateAndSendRates();

            // Assert
            verify(responseSpec, times(1)).bodyToMono(HttpResponseDto.class);
        }
    }

    @Test
    void testGenerateAndSendRates_shouldHandleUnknownError() {
        // Arrange
        var randomFloat = 12345f;

        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec<?> requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(mockWebClient.post()).thenReturn(requestBodySpec);
        when(requestBodySpec.uri("http://gateway/currencyExchangeService/rates")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.body(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(HttpResponseDto.class))
                .thenReturn(Mono.error(new RuntimeException("Unknown error")));

        try (var mocked = Mockito.mockStatic(RatesGenerationService.class)) {
            mocked.when(() -> RatesGenerationService.createRandomFloat()).thenReturn(Mono.just(randomFloat));

            ratesGenerationService.generateAndSendRates();
            verify(responseSpec, times(1)).bodyToMono(HttpResponseDto.class);
        }
    }
}