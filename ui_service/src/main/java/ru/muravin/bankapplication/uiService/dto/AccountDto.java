package ru.muravin.bankapplication.uiService.dto;

import lombok.Data;

@Data
public class AccountDto {
    private String accountNumber;
    private Float balance;
    private CurrencyDto currency;
}
