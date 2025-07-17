package com.example.bankcards.exception.cards;

public class CardNotFoundInBaseException extends RuntimeException {
    public CardNotFoundInBaseException(String message) {
        super(message);
    }
}
