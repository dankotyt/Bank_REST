package com.example.bankcards.dto.cards;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserCardRequest {
    @NotNull
    private Long userId;
}
