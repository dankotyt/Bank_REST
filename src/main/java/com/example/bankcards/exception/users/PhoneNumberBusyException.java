package com.example.bankcards.exception.users;

public class PhoneNumberBusyException extends RuntimeException {
    public PhoneNumberBusyException(String phoneNumber) {
        super("Phone number " + phoneNumber + " has already been used!");
    }
}
