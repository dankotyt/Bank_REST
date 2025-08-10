package com.example.bankcards.exception.auth;

public class InvalidPasswordException extends RuntimeException {
  public InvalidPasswordException() {
    super("Invalid password");
  }
}
