package ru.muravin.bankapplication.transferService.dto;

import lombok.Data;

@Data
public class AccountInfoDto {
    private String accountNumber;
    private String login;
    private CurrencyDto currency;
    private Float balance;
}
