package com.example.bankcards.exception.users;

public class EmailBusyException extends RuntimeException {
    public EmailBusyException(String email) {
        super("Email " + email + " has already been used!");
    }
}
