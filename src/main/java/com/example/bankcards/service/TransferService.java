package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.exception.cards.CardNotFoundException;
import com.example.bankcards.exception.cards.CardOperationException;
import com.example.bankcards.exception.users.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TransferService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public void transferBetweenUserCards(Long userId, String fromCardNumber,
                                         String toCardNumber, BigDecimal amount) {
        validateAmount(amount);

        //чтобы вводить последние 4 цифры
        String formatFrom = "**** **** **** " + fromCardNumber;
        String formatTo = "**** **** **** " + toCardNumber;

        Card fromCard = findUserCard(userId, formatFrom);
        Card toCard = findUserCard(userId, formatTo);

        validateTransfer(fromCard, toCard, amount);

        performTransfer(fromCard, toCard, amount);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CardOperationException("Amount must be positive");
        }
    }

    private void validateTransfer(Card fromCard, Card toCard, BigDecimal amount) {
        if (fromCard.getStatus() != CardStatus.ACTIVE || toCard.getStatus() != CardStatus.ACTIVE) {
            throw new CardOperationException("One of the cards is not active");
        }
        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new CardOperationException("Insufficient funds");
        }
    }

    private void performTransfer(Card fromCard, Card toCard, BigDecimal amount) {
        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));
        cardRepository.saveAll(List.of(fromCard, toCard));
    }

    private Card findUserCard(Long userId, String cardNumber) {
        //чтобы вводить последние 4 цифры
        String formatCardNumber = "**** **** **** " + cardNumber;
        var user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        return cardRepository.findByCardNumberAndUser(formatCardNumber, user)
                .orElseThrow(() -> new CardNotFoundException(cardNumber, user.getEmail()));
    }
}
