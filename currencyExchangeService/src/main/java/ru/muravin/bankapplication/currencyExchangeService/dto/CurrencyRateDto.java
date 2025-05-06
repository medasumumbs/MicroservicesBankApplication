package ru.muravin.bankapplication.currencyExchangeService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyRateDto {
    private String currencyCode;
    private Float buyRate;
    private Float sellRate;
}
