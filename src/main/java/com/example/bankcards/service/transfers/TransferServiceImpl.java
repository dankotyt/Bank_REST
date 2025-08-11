package com.example.bankcards.service.transfers;

import com.example.bankcards.dto.cards.TransferResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.cards.CardNotFoundException;
import com.example.bankcards.exception.cards.CardOperationException;
import com.example.bankcards.exception.users.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.Mapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Реализация сервиса для выполнения операций перевода средств между картами.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TransferServiceImpl implements  TransferService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final Mapper mapper;

    /**
     * {@inheritDoc}
     *
     * <p>Перед выполнением перевода выполняются следующие проверки:
     * <ul>
     *   <li>Сумма перевода должна быть положительной</li>
     *   <li>Обе карты должны быть активны</li>
     *   <li>На карте списания должно быть достаточно средств</li>
     * </ul>
     *
     * <p>Номера карт указываются последними 4 цифрами, полный номер формируется автоматически.
     */
    @Override
    public TransferResponse transferBetweenUserCards(Long userId, String fromCardNumber,
                                                     String toCardNumber, BigDecimal amount) {
        validateAmount(amount);

        //чтобы вводить последние 4 цифры
        String formatFrom = "**** **** **** " + fromCardNumber;
        String formatTo = "**** **** **** " + toCardNumber;

        Card fromCard = findUserCard(userId, formatFrom);
        Card toCard = findUserCard(userId, formatTo);

        validateTransfer(fromCard, toCard, amount);

        performTransfer(fromCard, toCard, amount);

        return new TransferResponse(
                mapper.toCardDTO(fromCard),
                mapper.toCardDTO(toCard)
        );
    }

    /**
     * Проверяет корректность суммы перевода.
     *
     * @param amount сумма для проверки
     * @throws CardOperationException если сумма меньше или равна нулю
     */
    private void validateAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CardOperationException("Amount must be positive");
        }
    }

    /**
     * Проверяет возможность выполнения перевода между картами.
     *
     * @param fromCard карта списания
     * @param toCard карта зачисления
     * @param amount сумма перевода
     * @throws CardOperationException если одна из карт неактивна или недостаточно средств
     */
    private void validateTransfer(Card fromCard, Card toCard, BigDecimal amount) {
        if (fromCard.getStatus() != CardStatus.ACTIVE || toCard.getStatus() != CardStatus.ACTIVE) {
            throw new CardOperationException("One of the cards is not active");
        }
        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new CardOperationException("Insufficient funds");
        }
    }

    /**
     * Выполняет перевод средств между картами.
     *
     * @param fromCard карта списания
     * @param toCard карта зачисления
     * @param amount сумма перевода
     */
    private void performTransfer(Card fromCard, Card toCard, BigDecimal amount) {
        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));
        cardRepository.saveAll(List.of(fromCard, toCard));
    }

    /**
     * Находит карту пользователя по номеру.
     *
     * @param userId идентификатор пользователя
     * @param cardNumber номер карты (полный формат)
     * @return найденная карта
     * @throws UserNotFoundException если пользователь не найден
     * @throws CardNotFoundException если карта не найдена у пользователя
     */
    private Card findUserCard(Long userId, String cardNumber) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        return cardRepository.findByCardNumberAndUser(cardNumber, user)
                .orElseThrow(() -> new CardNotFoundException(cardNumber, user.getEmail()));
    }
}

