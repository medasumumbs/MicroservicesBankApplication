package ru.muravin.bankapplication.accountsService.dto;

import lombok.Data;

@Data
public class AccountDto {
    private CurrencyDto currency;
    private String accountNumber;
    private Double balance;
}
