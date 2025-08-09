package com.example.bankcards.service.transfers;

import com.example.bankcards.dto.cards.TransferResponse;

import java.math.BigDecimal;

public interface TransferService {
    TransferResponse transferBetweenUserCards(Long userId, String fromCardNumber,
                                              String toCardNumber, BigDecimal amount);
}
