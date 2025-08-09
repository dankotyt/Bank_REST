package com.example.bankcards.dto.cards;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserCardOperationRequest {
    @NotNull
    private Long userId;

    @NotNull
    private String cardNumber;
}
