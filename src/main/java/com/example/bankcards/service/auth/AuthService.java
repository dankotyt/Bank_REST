package com.example.bankcards.service.auth;

import com.example.bankcards.dto.users.UserLoginRequest;
import com.example.bankcards.dto.users.UserLoginResponse;
import com.example.bankcards.dto.users.UserRegisterRequest;
import com.example.bankcards.exception.auth.InvalidPasswordException;
import com.example.bankcards.exception.auth.InvalidTokenException;
import com.example.bankcards.exception.users.UserExistsException;
import com.example.bankcards.exception.users.UserNotFoundException;

/**
 * Сервис для аутентификации и авторизации пользователей.
 * <p>
 * Предоставляет методы для:
 * <ul>
 *   <li>Регистрации новых пользователей</li>
 *   <li>Аутентификации существующих пользователей</li>
 *   <li>Управления токенами доступа</li>
 *   <li>Обновления токенов</li>
 * </ul>
 */
public interface AuthService {
    /**
     * Регистрирует нового пользователя в системе.
     *
     * @param userRegisterRequest данные для регистрации
     * @return ответ с токенами доступа и данными пользователя
     * @throws UserExistsException если пользователь с таким email или телефоном уже существует
     * @throws IllegalArgumentException если данные не прошли валидацию
     */
    UserLoginResponse register(UserRegisterRequest userRegisterRequest);

    /**
     * Аутентифицирует пользователя в системе.
     *
     * @param request данные для входа (email и пароль)
     * @return ответ с токенами доступа и данными пользователя
     * @throws UserNotFoundException если пользователь не найден
     * @throws InvalidPasswordException если введен неверный пароль
     */
    UserLoginResponse login(UserLoginRequest request);

    /**
     * Завершает сеанс пользователя, отзывая токены.
     *
     * @param refreshToken refresh token пользователя
     * @throws InvalidTokenException если токен недействителен
     */
    void logout(String refreshToken);

    /**
     * Обновляет пару токенов доступа.
     *
     * @param refreshToken текущий refresh token
     * @return новая пара токенов доступа
     * @throws InvalidTokenException если:
     *         - токен пустой
     *         - токен отозван
     *         - токен не соответствует пользователю
     *         - токен истек или невалиден
     * @throws UserNotFoundException если пользователь не найден
     */
    UserLoginResponse refreshToken(String refreshToken);
}
