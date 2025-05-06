package ru.muravin.bankapplication.currencyExchangeService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.muravin.bankapplication.currencyExchangeService.model.CurrencyRate;

public interface CurrencyRatesRepository extends JpaRepository<CurrencyRate, Long> {
}
