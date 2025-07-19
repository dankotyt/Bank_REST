package com.example.bankcards.dto.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginRequest {

    @Email(message = "Incorrect email!")
    private String email;
//    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Incorrect phone number!")
//    private String phoneNumber;
    @NotBlank
    private String password;
}
