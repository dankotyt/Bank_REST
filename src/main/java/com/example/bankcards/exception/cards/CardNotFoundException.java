package com.example.bankcards.exception.cards;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CardNotFoundException extends RuntimeException {
    public CardNotFoundException(String cardNumber, String email) {
        super("Card not found with number " + cardNumber + " for user with email " + email);
    }
}