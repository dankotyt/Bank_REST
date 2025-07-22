package com.example.bankcards.dto.cards;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Schema(description = "Запрос на перевод средств между картами")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransferRequest {
    @Schema(description = "Последние 4 цифры карты отправителя", example = "1234",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(min = 4, max = 4)
    private String fromCardNumber;

    @Schema(description = "Последние 4 цифры карты получателя", example = "5678",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(min = 4, max = 4)
    private String toCardNumber;

    @Schema(description = "Сумма перевода (больше 0)", example = "100.50",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
}
