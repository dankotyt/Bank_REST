package com.example.bankcards.dto.cards;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "Взаимодействие с картой пользователя")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserCardOperationRequest {
    @Schema(description = "ID пользователя", example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long userId;
    @Schema(description = "Последние 4 цифры карты пользователя", example = "7890",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private String cardNumber;
}
