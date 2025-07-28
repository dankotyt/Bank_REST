package com.example.bankcards.dto.cards;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Schema(description = "Ответ на запрос перевода средств")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransferResponse {
    @Schema(description = "Данные карты отправителя")
    private CardDTO fromCard;

    @Schema(description = "Данные карты получателя")
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
