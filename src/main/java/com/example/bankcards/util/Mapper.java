package com.example.bankcards.util;

import com.example.bankcards.dto.cards.CardDTO;
import com.example.bankcards.dto.users.UserDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import org.springframework.stereotype.Component;

@Component
public class Mapper {
    public UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getUserId());
        dto.setName(user.getName());
        dto.setSurname(user.getSurname());
        dto.setPatronymic(user.getPatronymic());
        dto.setBirthday(user.getBirthday());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        return dto;
    }

    public CardDTO toCardDTO(Card card) {
        CardDTO dto = new CardDTO();
        dto.setCardHolder(card.getCardHolder());
        dto.setCardNumber(card.getCardNumber());
        dto.setExpiryDate(card.getExpiryDate());
        dto.setBalance(card.getBalance());
        return dto;
    }
}
