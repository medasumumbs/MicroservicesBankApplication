package ru.muravin.bankapplication.notificationsService.serviceTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.muravin.bankapplication.exchangeGeneratorService.dto.CurrencyRateDto;
import ru.muravin.bankapplication.exchangeGeneratorService.kafka.RatesProducer;
import ru.muravin.bankapplication.exchangeGeneratorService.service.RatesGenerationService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatesGenerationServiceTest {

    @Mock
    private RatesProducer ratesProducer;

    @InjectMocks
    private RatesGenerationService ratesGenerationService;

    @Test
    void testCreateRandomFloat() {
        // Тестируем статический метод
        float random = RatesGenerationService.createRandomFloat();
        assertTrue(random >= 0 && random <= 1000); // System.currentTimeMillis()%100000 / 100.0
    }

    @Test
    void testGetCurrencyRateDtos() {
        float rate = 100.0f;
        List<CurrencyRateDto> rates = RatesGenerationService.getCurrencyRateDtos(rate);

        assertEquals(3, rates.size());

        var usd = rates.stream().filter(r -> r.getCurrencyCode().equals("USD")).findFirst().get();
        assertEquals(rate, usd.getBuyRate());
        assertEquals(rate - 3, usd.getSellRate());

        var eur = rates.stream().filter(r -> r.getCurrencyCode().equals("EUR")).findFirst().get();
        assertEquals(rate - 10, eur.getBuyRate());
        assertEquals(rate - 15, eur.getSellRate());

        var rub = rates.stream().filter(r -> r.getCurrencyCode().equals("RUB")).findFirst().get();
        assertEquals(1.0f, rub.getBuyRate());
        assertEquals(1.0f, rub.getSellRate());
    }

    @Test
    void testGenerateAndSendRates_Success() throws Exception {
        List<CurrencyRateDto> mockRates = getMockRates();

        when(ratesProducer.sendCurrencyRates(any())).thenReturn("OK");

        ratesGenerationService.generateAndSendRates(); // Вызываем метод

        verify(ratesProducer, times(1)).sendCurrencyRates(any());
    }

    @Test
    void testGenerateAndSendRates_Failure_RuntimeException() {
        List<CurrencyRateDto> mockRates = getMockRates();

        when(ratesProducer.sendCurrencyRates(any()))
                .thenThrow(new RuntimeException("Kafka error"));

        assertDoesNotThrow(() -> ratesGenerationService.generateAndSendRates());

        verify(ratesProducer, times(1)).sendCurrencyRates(any());
    }

    private List<CurrencyRateDto> getMockRates() {
        List<CurrencyRateDto> list = new ArrayList<>();
        CurrencyRateDto dto = new CurrencyRateDto();
        dto.setCurrencyCode("USD");
        dto.setBuyRate(100f);
        dto.setSellRate(97f);
        list.add(dto);
        return list;
    }
}