package ru.muravin.bankapplication.uiService.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CurrencyDto {
    private String currencyCode;
    private String title;
    public String getName() {
        return title;
    }
}


