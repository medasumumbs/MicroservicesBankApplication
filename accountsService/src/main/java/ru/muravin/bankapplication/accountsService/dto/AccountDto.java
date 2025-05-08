package ru.muravin.bankapplication.accountsService.dto;

import lombok.Data;

@Data
public class AccountDto {
    private String currencyCode;
    private String accountNumber;
    private Double balance;
}
