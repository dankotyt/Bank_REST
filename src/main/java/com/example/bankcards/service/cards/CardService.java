package com.example.bankcards.service.cards;

import com.example.bankcards.dto.cards.CardDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface CardService {
    Page<CardDTO> getUserCards(Long userId, String search, Pageable pageable);
    void blockCard(Long userId, String cardNumber);
    BigDecimal getCardBalance(Long userId, String cardNumber);
}

