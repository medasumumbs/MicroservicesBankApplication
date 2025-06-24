package ru.muravin.bankapplication.currencyExchangeService.service;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;
import ru.muravin.bankapplication.currencyExchangeService.dto.CurrencyRateDto;

import java.util.LinkedHashMap;
import java.util.List;

@Slf4j
@Service
public class CurrencyRatesConsumer {
    private final CurrencyRatesService currencyRatesService;
    private final MeterRegistry meterRegistry;

    @Value("${metricsEnabled:true}")
    private boolean metricsEnabled;

    public CurrencyRatesConsumer(CurrencyRatesService currencyRatesService, MeterRegistry meterRegistry) {
        this.currencyRatesService = currencyRatesService;
        this.meterRegistry = meterRegistry;
    }

    @RetryableTopic(backoff = @Backoff(500), timeout = "4000")
    @KafkaListener(topics = "currency-rates", groupId = "currency-rates-grp")
    public void consume(List<LinkedHashMap> currencyRateDtoLinkedHashMap, Acknowledgment ack) {
        log.info("Consuming currency rates: {}", currencyRateDtoLinkedHashMap);
        var currencyRateDtoList = currencyRateDtoLinkedHashMap.stream().map(linkedHashMap -> {
            CurrencyRateDto currencyRateDto = new CurrencyRateDto();
            currencyRateDto.setBuyRate(Float.parseFloat(((Double) linkedHashMap.get("buyRate")).toString()));
            currencyRateDto.setSellRate(Float.parseFloat(((Double) linkedHashMap.get("sellRate")).toString()));
            currencyRateDto.setCurrencyCode((String) linkedHashMap.get("currencyCode"));
            return currencyRateDto;
        }).toList();
        log.info("CurrencyRatesConsumer called: {}", currencyRateDtoList);
        ack.acknowledge();
        log.info("CurrencyRatesConsumer acknowledged");
        currencyRatesService.saveRates(currencyRateDtoList);
        log.info("CurrencyRatesConsumer saved rates");
        if (metricsEnabled) meterRegistry.counter("currency_rates_updated").increment();
    }
}
