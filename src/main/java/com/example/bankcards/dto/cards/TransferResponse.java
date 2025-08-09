package com.example.bankcards.dto.cards;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransferResponse {
    private CardDTO fromCard;
    private CardDTO toCard;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransferResponse that = (TransferResponse) o;
        return Objects.equals(fromCard, that.fromCard) &&
                Objects.equals(toCard, that.toCard);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromCard, toCard);
    }
}
