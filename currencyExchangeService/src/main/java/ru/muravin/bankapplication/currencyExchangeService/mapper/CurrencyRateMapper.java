package ru.muravin.bankapplication.currencyExchangeService.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.muravin.bankapplication.currencyExchangeService.dto.CurrencyRateDto;
import ru.muravin.bankapplication.currencyExchangeService.model.CurrencyRate;

@Mapper(componentModel = "spring")
public interface CurrencyRateMapper {
    CurrencyRate toEntity(CurrencyRateDto dto);
    CurrencyRateDto toDto(CurrencyRate currencyRate);
}
