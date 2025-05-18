package ru.muravin.bankapplication.antifraudService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HttpResponseDto {
    private String statusCode;
    private String statusMessage;
}
