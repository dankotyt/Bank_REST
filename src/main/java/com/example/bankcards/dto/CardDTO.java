package com.example.bankcards.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CardDTO {
    @JsonProperty("cardNumber")
    private String cardNumber;

    @JsonProperty("expiryDate")
    private LocalDate expiryDate;

    @JsonProperty("cardHolder")
    private String cardHolder;

    @JsonProperty("balance")
    private BigDecimal balance;
}
