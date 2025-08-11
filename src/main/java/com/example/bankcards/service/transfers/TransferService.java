package com.example.bankcards.service.transfers;

import com.example.bankcards.dto.cards.TransferResponse;
import com.example.bankcards.exception.cards.CardNotFoundException;
import com.example.bankcards.exception.cards.CardOperationException;
import com.example.bankcards.exception.users.UserNotFoundException;

import java.math.BigDecimal;

/**
 * Сервис для выполнения операций перевода средств между картами пользователя.
 */
public interface TransferService {
    /**
     * Выполняет перевод средств между картами одного пользователя.
     *
     * @param userId идентификатор пользователя, выполняющего перевод
     * @param fromCardNumber номер карты списания (последние 4 цифры)
     * @param toCardNumber номер карты зачисления (последние 4 цифры)
     * @param amount сумма перевода
     * @return объект {@link TransferResponse} с информацией о картах после перевода
     * @throws CardOperationException если сумма перевода некорректна, одна из карт неактивна
     *                               или недостаточно средств на карте списания
     * @throws UserNotFoundException если пользователь с указанным ID не найден
     * @throws CardNotFoundException если одна из карт не найдена у указанного пользователя
     */
    TransferResponse transferBetweenUserCards(Long userId, String fromCardNumber,
                                              String toCardNumber, BigDecimal amount);
}
