package ru.muravin.bankapplication.antifraudService.dto;

import lombok.Data;

@Data
public class OperationDto {
    private String currencyCode;
    private String fromCurrency;
    private String toCurrency;
    private String login;
    private String action;
    private String amount;
    private String fromAccount;
    private String toAccount;
}
