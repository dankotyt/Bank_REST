package com.example.bankcards.dto.cards;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransferRequest {
    @NotBlank
    @Size(min = 4, max = 4)
    private String fromCardNumber;

    @NotBlank
    @Size(min = 4, max = 4)
    private String toCardNumber;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
}
