package com.example.bankcards.dto.cards;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Schema(description = "Запрос на изменение баланса карты")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CardReplenishmentRequest {
    @Schema(description = "ID пользователя", example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    @Schema(description = "Номер карты", example = "**** **** **** 7890",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String cardNumber;

    @Schema(description = "Новый баланс", example = "1000.00",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal balance;
}
