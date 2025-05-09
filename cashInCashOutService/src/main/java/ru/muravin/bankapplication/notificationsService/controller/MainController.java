package ru.muravin.bankapplication.notificationsService.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.muravin.bankapplication.notificationsService.dto.CashInCashOutDto;
import ru.muravin.bankapplication.notificationsService.dto.HttpResponseDto;

@RestController
@RequestMapping
public class MainController {

    @PostMapping(value = "/withdrawCash") //username="+login+"&currency="+currency+"&amount="+amount
    public ResponseEntity<HttpResponseDto> withdrawCash(@RequestBody CashInCashOutDto cashInCashOutDto) {
        var action = cashInCashOutDto.getAction();
        if (action.equals("GET")) {
            return ResponseEntity.ok(new HttpResponseDto("OK","Деньги успешно списаны"));
        }
        if (action.equals("PUT")) {
            return ResponseEntity.ok(new HttpResponseDto("OK","Деньги успешно зачислены"));
        }
        return ResponseEntity.notFound().build();
    }
    @ExceptionHandler
    public HttpResponseDto handleException(Exception ex) {
        ex.printStackTrace();
        return new HttpResponseDto("ERROR",ex.getLocalizedMessage());
    }
}
