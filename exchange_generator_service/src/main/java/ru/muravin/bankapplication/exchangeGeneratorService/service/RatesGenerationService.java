package ru.muravin.bankapplication.exchangeGeneratorService.service;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.muravin.bankapplication.exchangeGeneratorService.dto.CurrencyRateDto;
import ru.muravin.bankapplication.exchangeGeneratorService.kafka.RatesProducer;

import java.util.ArrayList;

@Service
@Slf4j
public class RatesGenerationService {

    private final RatesProducer ratesProducer;
    @Setter
    @Value("${gatewayHost:gateway}")
    private String gatewayHost;

    public RatesGenerationService(RatesProducer ratesProducer) {
        this.ratesProducer = ratesProducer;
    }

    public static Float createRandomFloat() {
        var currentTimeMillisLast5Digits = System.currentTimeMillis() % 100000;
        return (float) (currentTimeMillisLast5Digits / 100.0);
    }

    @Scheduled(fixedRate = 5000)
    public void generateAndSendRates() {
        log.info("Sending random rate to ExchangeGeneratorService");
        var rates = getCurrencyRateDtos(createRandomFloat());
        String result;
        try {
            result = ratesProducer.sendCurrencyRates(rates);
        } catch (Exception e) {
            result = e.getMessage();
        }
        if (!result.equals("OK")) {
            log.error("Failed to send rates to ExchangeGeneratorService: " + result);
        } else {
            log.info("Successfully sent rate to ExchangeGeneratorService");
        }
    }

    public static ArrayList<CurrencyRateDto> getCurrencyRateDtos(Float randomFloat) {
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
        return rates;
    }

}
