package ru.muravin.bankapplication.currencyExchangeService.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;
import ru.muravin.bankapplication.currencyExchangeService.dto.CurrencyRateDto;

import java.util.List;

@Slf4j
@Service
public class CurrencyRatesConsumer {
    private final CurrencyRatesService currencyRatesService;

    public CurrencyRatesConsumer(CurrencyRatesService currencyRatesService) {
        this.currencyRatesService = currencyRatesService;
    }

    @RetryableTopic(backoff = @Backoff(500), timeout = "4000")
    @KafkaListener(topics = "currency-rates", groupId = "currency-rates-grp")
    public void consume(List<CurrencyRateDto> currencyRateDtoList, Acknowledgment ack) {
        log.info("CurrencyRatesConsumer called: {}", currencyRateDtoList);
        ack.acknowledge();
        log.info("CurrencyRatesConsumer acknowledged");
        currencyRatesService.saveRates(currencyRateDtoList);
        log.info("CurrencyRatesConsumer saved rates");
    }
}
