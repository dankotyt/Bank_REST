package com.example.bankcards.service.admin;

import com.example.bankcards.dto.cards.CardDTO;
import com.example.bankcards.dto.users.UpdateUserRequest;
import com.example.bankcards.dto.users.UserDTO;
import com.example.bankcards.dto.users.UserRegisterRequest;
import com.example.bankcards.exception.cards.CardNotFoundException;
import com.example.bankcards.exception.cards.CardOperationException;
import com.example.bankcards.exception.users.EmailBusyException;
import com.example.bankcards.exception.users.PhoneNumberBusyException;
import com.example.bankcards.exception.users.UserExistsException;
import com.example.bankcards.exception.users.UserNotFoundException;

import java.math.BigDecimal;
import java.util.List;

/**
 * Сервис для административных операций с пользователями и картами.
 * <p>
 * Предоставляет методы для управления:
 * <ul>
 *   <li>Банковскими картами (создание, активация, блокировка, переводы)</li>
 *   <li>Пользователями (CRUD операции)</li>
 * </ul>
 */
public interface AdminService {
    /**
     * Создает новую карту для указанного пользователя.
     *
     * @param userId идентификатор пользователя
     * @return DTO созданной карты
     * @throws UserNotFoundException если пользователь не найден
     */
    CardDTO createCard(Long userId);

    /**
     * Активирует карту пользователя.
     *
     * @param userId идентификатор пользователя
     * @param cardNumber последние 4 цифры номера карты
     * @return DTO обновленной карты
     * @throws UserNotFoundException если пользователь не найден
     * @throws CardNotFoundException если карта не найдена
     * @throws CardOperationException если карта уже активна
     */
    CardDTO setActiveStatus(Long userId, String cardNumber);

    /**
     * Блокирует карту пользователя.
     *
     * @param userId идентификатор пользователя
     * @param cardNumber последние 4 цифры номера карты
     * @return DTO обновленной карты
     * @throws UserNotFoundException если пользователь не найден
     * @throws CardNotFoundException если карта не найдена
     * @throws CardOperationException если карта уже заблокирована
     */
    CardDTO blockCard(Long userId, String cardNumber);

    /**
     * Удаляет карту пользователя.
     *
     * @param userId идентификатор пользователя
     * @param cardNumber последние 4 цифры номера карты
     * @throws UserNotFoundException если пользователь не найден
     * @throws CardNotFoundException если карта не найдена
     */
    void deleteCard(Long userId, String cardNumber);

    /**
     * Обновляет баланс карты пользователя.
     *
     * @param userId идентификатор пользователя
     * @param cardNumber последние 4 цифры номера карты
     * @param balance новое значение баланса
     * @return DTO обновленной карты
     * @throws UserNotFoundException если пользователь не найден
     * @throws CardNotFoundException если карта не найдена
     */
    CardDTO updateUserBalance(Long userId, String cardNumber, BigDecimal balance);

    /**
     * Возвращает список всех карт в системе.
     *
     * @return список DTO карт
     */
    List<CardDTO> getAllCards();

    /**
     * Возвращает список карт указанного пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список DTO карт пользователя
     * @throws UserNotFoundException если пользователь не найден
     */
    List<CardDTO> getUserCards(Long userId);

    /**
     * Возвращает список всех пользователей.
     *
     * @return список DTO пользователей
     */
    List<UserDTO> getAllUsers();

    /**
     * Находит пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return DTO пользователя
     * @throws UserNotFoundException если пользователь не найден
     */
    UserDTO getUserById(Long userId);

    /**
     * Находит пользователя по email.
     *
     * @param email email пользователя
     * @return DTO пользователя
     * @throws UserNotFoundException если пользователь не найден
     */
    UserDTO getUserByEmail(String email);

    /**
     * Находит пользователя по номеру телефона.
     *
     * @param phone номер телефона
     * @return DTO пользователя
     * @throws UserNotFoundException если пользователь не найден
     */
    UserDTO getUserByPhone(String phone);

    /**
     * Создает нового пользователя.
     *
     * @param request данные для регистрации
     * @return DTO созданного пользователя
     * @throws UserExistsException если пользователь с таким email/телефоном уже существует
     * @throws IllegalArgumentException если данные не прошли валидацию
     */
    UserDTO createUser(UserRegisterRequest request);

    /**
     * Обновляет данные пользователя.
     *
     * @param userId идентификатор пользователя
     * @param request новые данные пользователя
     * @return DTO обновленного пользователя
     * @throws UserNotFoundException если пользователь не найден
     * @throws EmailBusyException если новый email уже занят
     * @throws PhoneNumberBusyException если новый номер телефона уже занят
     */
    UserDTO updateUser(Long userId, UpdateUserRequest request);

    /**
     * Удаляет пользователя.
     *
     * @param userId идентификатор пользователя
     * @throws UserNotFoundException если пользователь не найден
     */
    void deleteUser(Long userId);
}