package com.example.bankcards.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class UserLoginRequest {

    @Email(message = "Incorrect email!")
    private String email;
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Incorrect phone number!")
    private String phoneNumber;
    @NotBlank
    private String password;

    public boolean isValid() {
        return (email != null) ^ (phoneNumber != null); // XOR - либо email, либо phone
    }
}
