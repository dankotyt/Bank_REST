package com.example.bankcards.service.cards;

import com.example.bankcards.dto.cards.CardDTO;
import com.example.bankcards.exception.cards.CardNotFoundException;
import com.example.bankcards.exception.cards.CardOperationException;
import com.example.bankcards.exception.users.UserNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

/**
 * Сервис для работы с банковскими картами пользователей.
 * <p>
 * Предоставляет методы для:
 * <ul>
 *   <li>Получения списка карт с возможностью поиска и пагинации</li>
 *   <li>Блокировки карт</li>
 *   <li>Проверки баланса</li>
 * </ul>
 */
public interface CardService {
    /**
     * Возвращает страницу с картами пользователя, отфильтрованными по поисковому запросу.
     *
     * @param userId идентификатор пользователя
     * @param search поисковый запрос (по номеру или имени держателя карты), может быть null
     * @param pageable параметры пагинации
     * @return страница с DTO карт пользователя
     * @throws UserNotFoundException если пользователь не найден
     */
    Page<CardDTO> getUserCards(Long userId, String search, Pageable pageable);

    /**
     * Блокирует карту пользователя.
     *
     * @param userId идентификатор пользователя
     * @param cardNumber последние 4 цифры номера карты
     * @throws UserNotFoundException если пользователь не найден
     * @throws CardNotFoundException если карта не найдена
     * @throws CardOperationException если карта уже заблокирована
     */
    void blockCard(Long userId, String cardNumber);

    /**
     * Возвращает текущий баланс карты пользователя.
     *
     * @param userId идентификатор пользователя
     * @param cardNumber последние 4 цифры номера карты
     * @return текущий баланс карты
     * @throws UserNotFoundException если пользователь не найден
     * @throws CardNotFoundException если карта не найдена
     */
    BigDecimal getCardBalance(Long userId, String cardNumber);
}

