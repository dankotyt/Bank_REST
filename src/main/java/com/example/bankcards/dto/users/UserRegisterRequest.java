package com.example.bankcards.dto.users;

import com.example.bankcards.entity.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterRequest {
    @NotNull(message = "Имя обязательно")
    private String name;

    @NotNull(message = "Фамилия обязательна")
    private String surname;

    private String patronymic;

    @Past
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    @NotNull
    @Email
    private String email;

    @NotNull
    @Size(min=8, max=64)
    private String password;
    private Role role;
}
