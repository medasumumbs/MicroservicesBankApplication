package ru.muravin.bankapplication.uiService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HttpResponseDto {
    private String statusCode;
    private String statusMessage;
}
