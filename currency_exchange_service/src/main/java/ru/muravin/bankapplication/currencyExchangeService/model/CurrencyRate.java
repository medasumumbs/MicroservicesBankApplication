package ru.muravin.bankapplication.currencyExchangeService.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "currency_rates")
public class CurrencyRate {
    @Id
    private String currencyCode;
    private Float buyRate;
    private Float sellRate;
}
