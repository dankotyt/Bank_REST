package com.example.bankcards.dto.cards;

import com.example.bankcards.entity.CardStatus;
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
    private String cardNumber;
    private LocalDate expiryDate;
    private String cardHolder;
    private BigDecimal balance;
    private CardStatus status;
}
