package ru.muravin.bankapplication.antifraudService.controller;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.muravin.bankapplication.antifraudService.dto.OperationDto;
import ru.muravin.bankapplication.antifraudService.dto.HttpResponseDto;

@RestController
@RequestMapping
public class MainController {

    private final MeterRegistry meterRegistry;

    @Value("${metricsEnabled:true}")
    private boolean metricsEnabled;

    @Autowired
    public MainController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @PostMapping(value = "/checkOperations")
    public ResponseEntity<HttpResponseDto> CheckOperation(@RequestBody OperationDto operationDto) {
        var action = operationDto.getAction();
        if (action == null) action = "";
        var amount = Float.parseFloat(operationDto.getAmount());
        if (amount > 100000) {
            saveBlockedOperationMetric(operationDto);
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
        saveBlockedOperationMetric(operationDto);
        return ResponseEntity.notFound().build();
    }
    @ExceptionHandler
    public HttpResponseDto handleException(Exception ex) {
        ex.printStackTrace();
        return new HttpResponseDto("ERROR",ex.getLocalizedMessage());
    }

    private void saveBlockedOperationMetric(OperationDto operationDto) {
        if (!metricsEnabled) return;
        if (operationDto.getAction().equals("GET")) {
            operationDto.setFromAccount(operationDto.getLogin() + "_" + operationDto.getCurrencyCode());
            operationDto.setToAccount("NONE");
        }
        if (operationDto.getAction().equals("PUT")) {
            operationDto.setToAccount(operationDto.getLogin() + "_" + operationDto.getCurrencyCode());
            operationDto.setFromAccount("NONE");
        }
        meterRegistry.counter(
                "operationBlocked",
                "login", operationDto.getLogin(),
                "recipientAccount", operationDto.getToAccount(),
                "senderAccount", operationDto.getFromAccount()).increment();
    }
}
