package com.example.bankcards.dto.users;

import com.example.bankcards.entity.Role;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class UserRegisterRequest {
    @NotNull(message = "The field couldn`t be empty!")
    private String name;

    @NotNull(message = "The field couldn`t be empty!")
    private String surname;

    private String patronymic;

    @Past
    @NotNull(message = "The field couldn`t be empty!")
    private LocalDate birthday;

    @NotNull(message = "The field couldn`t be empty!")
    @Email(message = "Некорректный email!")
    private String email;

    @NotNull(message = "The phone number couldn`t be empty!")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Incorrect phone number!")
    private String phoneNumber;

    @NotNull(message = "The field couldn`t be empty!")
    @Size(min=8, max=64)
    private String password;

    private Role role = Role.USER;
}
