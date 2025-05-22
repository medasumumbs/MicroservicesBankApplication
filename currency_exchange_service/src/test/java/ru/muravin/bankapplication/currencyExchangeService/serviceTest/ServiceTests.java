package ru.muravin.bankapplication.currencyExchangeService.serviceTest;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.utility.TestcontainersConfiguration;
import ru.muravin.bankapplication.currencyExchangeService.CurrencyExchangeServiceApplication;
import ru.muravin.bankapplication.currencyExchangeService.configurations.OAuth2SecurityConfig;
import ru.muravin.bankapplication.currencyExchangeService.dto.CurrencyRateDto;
import ru.muravin.bankapplication.currencyExchangeService.model.CurrencyRate;
import ru.muravin.bankapplication.currencyExchangeService.service.CurrencyRatesService;
import ru.muravin.bankapplication.currencyExchangeService.mapper.CurrencyRateMapper;
import ru.muravin.bankapplication.currencyExchangeService.repository.CurrencyRatesRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=update",
        "spring.liquibase.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration," +
                "org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration"
},
        classes = {CurrencyExchangeServiceApplication.class})
@TestPropertySource(locations = "classpath:application.yml")
@AutoConfigureWebTestClient
@ExtendWith(MockitoExtension.class)
public class ServiceTests {

    @MockitoBean
    private OAuth2SecurityConfig oAuth2SecurityConfig;

    @MockitoBean
    private JwtAuthenticationConverter jwtAuthenticationConverter;

    @MockitoBean
    private CurrencyRateMapper currencyRateMapper;

    @MockitoBean
    private CurrencyRatesRepository currencyRatesRepository;

    @Autowired
    private CurrencyRatesService currencyRatesService;

    @Test
    void testSaveRates_ShouldMapAndSaveEntities() {
        // Given
        CurrencyRateDto rateDto1 = new CurrencyRateDto("USD", 1.0f, 1f);
        CurrencyRateDto rateDto2 = new CurrencyRateDto("EUR", 0.9f, 0.9f);

        CurrencyRate entity1 = new CurrencyRate();
        entity1.setCurrencyCode("USD");
        entity1.setSellRate(1.0f);
        entity1.setBuyRate(1.0f);

        CurrencyRate entity2 = new CurrencyRate();
        entity2.setCurrencyCode("EUR");
        entity1.setSellRate(0.9f);
        entity1.setBuyRate(0.9f);

        // When
        when(currencyRateMapper.toEntity(rateDto1)).thenReturn(entity1);
        when(currencyRateMapper.toEntity(rateDto2)).thenReturn(entity2);

        currencyRatesService.saveRates(List.of(rateDto1, rateDto2));

        // Then
        verify(currencyRatesRepository, times(1)).saveAll(List.of(entity1, entity2));
    }

    @Test
    void testFindAll_ShouldReturnMappedDtos() {
        // Given
        CurrencyRate entity1 = new CurrencyRate();
        entity1.setBuyRate(1.0f);
        entity1.setCurrencyCode("USD");
        entity1.setSellRate(1.0f);

        CurrencyRate entity2 = new CurrencyRate();
        entity2.setBuyRate(0.9f);
        entity2.setCurrencyCode("EUR");
        entity2.setSellRate(0.9f);

        CurrencyRateDto dto1 = new CurrencyRateDto("USD", 1.0f, 1.0f);
        CurrencyRateDto dto2 = new CurrencyRateDto("EUR", 0.9f, 0.9f);

        // When
        when(currencyRatesRepository.findAll()).thenReturn(List.of(entity1, entity2));
        when(currencyRateMapper.toDto(entity1)).thenReturn(dto1);
        when(currencyRateMapper.toDto(entity2)).thenReturn(dto2);

        List<CurrencyRateDto> result = currencyRatesService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(dto1));
        assertTrue(result.contains(dto2));
        verify(currencyRateMapper, times(2)).toDto(any(CurrencyRate.class));
    }
}
