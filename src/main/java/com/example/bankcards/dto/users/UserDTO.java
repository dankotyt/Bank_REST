package com.example.bankcards.dto.users;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Данные пользователя")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    @Schema(description = "ID пользователя", example = "1")
    private Long id;

    @Schema(description = "Имя", example = "Иван")
    private String name;

    @Schema(description = "Фамилия", example = "Иванов")
    private String surname;

    @Schema(description = "Отчество (опционально)", example = "Иванович")
    private String patronymic;

    @Schema(description = "Дата рождения (ГГГГ-ММ-ДД)", example = "1990-01-01")
    private LocalDate birthday;

    @Schema(description = "Email", example = "user@example.com")
    private String email;

    @Schema(description = "Номер телефона", example = "+79123456789")
    private String phoneNumber;

    @Schema(description = "Дата регистрации", example = "2023-01-15T12:00:00")
    private LocalDateTime createdAt;
}
