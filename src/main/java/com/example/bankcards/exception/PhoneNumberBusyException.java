package com.example.bankcards.exception;

public class PhoneNumberBusyException extends RuntimeException {
    public PhoneNumberBusyException(String message) {
        super(message);
    }
}
