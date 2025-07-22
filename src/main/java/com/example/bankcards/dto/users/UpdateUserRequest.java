package com.example.bankcards.dto.users;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Schema(description = "Запрос на обновление данных пользователя")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @Schema(
            description = "Имя пользователя",
            example = "Иван",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 100
    )
    @NotBlank
    @Size(max = 100)
    private String name;

    @Schema(
            description = "Фамилия пользователя",
            example = "Иванов",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 100
    )
    @NotBlank
    @Size(max = 100)
    private String surname;

    @Schema(
            description = "Отчество (опционально)",
            example = "Иванович",
            maxLength = 100,
            nullable = true
    )
    @Size(max = 100)
    private String patronymic;

    @Schema(
            description = "Дата рождения (ГГГГ-ММ-ДД)",
            example = "1990-01-01",
            type = "string",
            format = "date",
            nullable = true
    )
    @Past
    private LocalDate birthday;

    @Schema(
            description = "Email пользователя",
            example = "user@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Email
    @NotBlank
    private String email;

    @Schema(
            description = "Номер телефона (формат: +79123456789)",
            example = "+79123456789",
            pattern = "^\\+?[0-9]{10,15}$",
            nullable = true
    )
    @Pattern(regexp = "^\\+?[0-9]{10,15}$")
    private String phoneNumber;
}
