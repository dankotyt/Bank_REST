package com.example.bankcards.dto.cards;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "Запрос карты с ID пользователя")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserCardRequest {
    @Schema(description = "ID пользователя", example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long userId;
}
