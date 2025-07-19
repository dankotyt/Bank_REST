package com.example.bankcards.dto.cards;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransferResponse {
    private CardDTO fromCard;
    private CardDTO toCard;
}
