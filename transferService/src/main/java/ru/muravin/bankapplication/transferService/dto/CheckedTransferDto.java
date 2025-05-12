package ru.muravin.bankapplication.transferService.dto;

import lombok.Data;

@Data
public class CheckedTransferDto {
    private String fromAccountNumber;
    private String toAccountNumber;
    private Float amountFrom;
    private Float amountTo;
}
