package ru.muravin.bankapplication.currencyExchangeService.service;

import brave.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.muravin.bankapplication.currencyExchangeService.dto.CurrencyRateDto;
import ru.muravin.bankapplication.currencyExchangeService.mapper.CurrencyRateMapper;
import ru.muravin.bankapplication.currencyExchangeService.repository.CurrencyRatesRepository;

import java.util.List;

@Service
@Slf4j
public class CurrencyRatesService {
    private final CurrencyRateMapper currencyRateMapper;
    private final CurrencyRatesRepository notificationsRepository;

    private final Tracer tracer;

    @Autowired
    public CurrencyRatesService(CurrencyRateMapper currencyRateMapper, CurrencyRatesRepository notificationsRepository, Tracer tracer) {
        this.currencyRateMapper = currencyRateMapper;
        this.notificationsRepository = notificationsRepository;
        this.tracer = tracer;
    }

    public void saveRates(List<CurrencyRateDto> ratesList) {
        log.info("Saving rate list");
        var span = tracer.nextSpan().name("db Saving rate list").start();
        notificationsRepository.saveAll(ratesList.stream().map(currencyRateMapper::toEntity).toList());
        span.finish();
    }

    public List<CurrencyRateDto> findAll() {
        log.info("Finding rate list");
        var span = tracer.nextSpan().name("db Finding rate list").start();
        var result = notificationsRepository.findAll().stream().map(currencyRateMapper::toDto).toList();
        span.finish();
        return result;
    }
}
