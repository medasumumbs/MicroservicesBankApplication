package ru.muravin.bankapplication.currencyExchangeService.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.muravin.bankapplication.currencyExchangeService.dto.CurrencyRateDto;
import ru.muravin.bankapplication.currencyExchangeService.dto.HttpResponseDto;
import ru.muravin.bankapplication.currencyExchangeService.service.CurrencyRatesService;

import java.util.List;

@RestController
@RequestMapping("/rates")
public class RatesController {
    private final CurrencyRatesService notificationsService;

    @Autowired
    public RatesController(CurrencyRatesService notificationsService) {
        this.notificationsService = notificationsService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HttpResponseDto receiveRates(
            @RequestBody List<CurrencyRateDto> ratesList) {
        notificationsService.saveRates(ratesList);
        return new HttpResponseDto("OK","Rates received");
    }

    @ExceptionHandler
    public HttpResponseDto handleException(Exception ex) {
        ex.printStackTrace();
        return new HttpResponseDto("ERROR",ex.getLocalizedMessage());
    }
}
