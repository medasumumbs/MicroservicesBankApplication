package ru.muravin.bankapplication.cashInCashOutService.dto;

import lombok.Data;

@Data
public class OperationDto {
    private String currencyCode;
    private String login;
    private String action;
    private String amount;
    private String fromAccount;
    private String toAccount;
}
