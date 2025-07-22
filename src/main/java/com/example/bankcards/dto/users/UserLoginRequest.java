package com.example.bankcards.dto.users;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Запрос на авторизацию пользователя")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginRequest {

    @Schema(
            description = "Email пользователя",
            example = "user@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Email(message = "Некорректный формат email")
    private String email;

    @Schema(
            description = "Пароль",
            example = "mySecurePassword123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    private String password;
}
