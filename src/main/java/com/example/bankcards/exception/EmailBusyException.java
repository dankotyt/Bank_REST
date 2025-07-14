package com.example.bankcards.exception;

public class EmailBusyException extends RuntimeException {
    public EmailBusyException(String message) {
        super(message);
    }
}
