package com.example.bankcards.dto.cards;

import com.example.bankcards.entity.CardStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Данные банковской карты")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CardDTO {
    @Schema(description = "Номер карты", example = "**** **** **** 7890")
    private String cardNumber;

    @Schema(description = "Дата окончания действия (ГГГГ-ММ-ДД)", example = "2025-12-31")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;

    @Schema(description = "Имя держателя карты", example = "IVAN IVANOV")
    private String cardHolder;

    @Schema(description = "Текущий баланс", example = "1500.75")
    private BigDecimal balance;

    @Schema(description = "Статус карты", example = "ACTIVE",
            allowableValues = {"ACTIVE", "BLOCKED", "EXPIRED"})
    private CardStatus status;
}
