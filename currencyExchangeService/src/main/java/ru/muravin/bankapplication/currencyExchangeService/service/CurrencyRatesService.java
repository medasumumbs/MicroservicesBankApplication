package ru.muravin.bankapplication.currencyExchangeService.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.muravin.bankapplication.currencyExchangeService.dto.CurrencyRateDto;
import ru.muravin.bankapplication.currencyExchangeService.mapper.CurrencyRateMapper;
import ru.muravin.bankapplication.currencyExchangeService.model.CurrencyRate;
import ru.muravin.bankapplication.currencyExchangeService.repository.CurrencyRatesRepository;

import java.util.List;

@Service
public class CurrencyRatesService {
    private final CurrencyRateMapper currencyRateMapper;
    private final CurrencyRatesRepository notificationsRepository;

    @Autowired
    public CurrencyRatesService(CurrencyRateMapper currencyRateMapper, CurrencyRatesRepository notificationsRepository) {
        this.currencyRateMapper = currencyRateMapper;
        this.notificationsRepository = notificationsRepository;
    }

    public void saveRates(List<CurrencyRateDto> ratesList) {
        notificationsRepository.saveAll(ratesList.stream().map(currencyRateMapper::toEntity).toList());
    }
}
