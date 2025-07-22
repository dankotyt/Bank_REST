package com.example.bankcards.dto.users;

import com.example.bankcards.entity.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Schema(description = "Запрос на регистрацию пользователя")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterRequest {
    @Schema(description = "Имя", example = "Иван",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Имя обязательно")
    private String name;

    @Schema(description = "Фамилия", example = "Иванов",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Фамилия обязательна")
    private String surname;

    @Schema(description = "Отчество (опционально)", example = "Иванович")
    private String patronymic;

    @Schema(description = "Дата рождения (ГГГГ-ММ-ДД)", example = "1990-01-01",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @Past
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    @Schema(description = "Email", example = "user@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @Email
    private String email;

    @Schema(description = "Номер телефона", example = "+79123456789",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @Pattern(regexp = "^\\+?[0-9]{10,15}$")
    private String phoneNumber;

    @Schema(description = "Пароль (8-64 символов)", example = "securePassword123!",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @Size(min=8, max=64)
    private String password;

    @Schema(description = "Роль пользователя", example = "USER",
            allowableValues = {"USER", "ADMIN"})
    private Role role;
}
