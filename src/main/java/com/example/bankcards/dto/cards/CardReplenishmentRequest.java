package com.example.bankcards.dto.cards;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CardReplenishmentRequest {
    private Long userId;
    private String cardNumber;
    private BigDecimal balance;
}
