package ru.muravin.bankapplication.antifraudService.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.muravin.bankapplication.antifraudService.dto.OperationDto;
import ru.muravin.bankapplication.antifraudService.dto.HttpResponseDto;

@RestController
@RequestMapping
public class MainController {

    @PostMapping(value = "/checkOperations")
    public ResponseEntity<HttpResponseDto> CheckOperation(@RequestBody OperationDto operationDto) {
        var action = operationDto.getAction();
        if (action == null) action = "";
        var amount = Float.parseFloat(operationDto.getAmount());
        if (amount > 100000) {
            return ResponseEntity.ok(new HttpResponseDto("FRAUD","Слишком большая сумма"));
        }
        if (action.equals("GET")) {
            return ResponseEntity.ok(new HttpResponseDto("OK","Деньги можно снять"));
        }
        if (action.equals("PUT")) {
            return ResponseEntity.ok(new HttpResponseDto("OK","Деньги могут быть зачислить на счёт"));
        }
        if (action.equals("TRANSFER")) {
            return ResponseEntity.ok(new HttpResponseDto("OK","Деньги могут быть переведены"));
        }
        return ResponseEntity.notFound().build();
    }
    @ExceptionHandler
    public HttpResponseDto handleException(Exception ex) {
        ex.printStackTrace();
        return new HttpResponseDto("ERROR",ex.getLocalizedMessage());
    }
}
