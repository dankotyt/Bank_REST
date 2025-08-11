package com.example.bankcards.security.validator;

import com.example.bankcards.entity.User;

/**
 * Валидатор JWT токенов.
 * Проверяет валидность токенов и управляет списком отозванных токенов.
 */
public interface JwtTokenValidator {

    /**
     * Проверяет валидность токена для указанного пользователя.
     *
     * @param token JWT токен
     * @param user пользователь, для которого проверяется токен
     * @return true, если токен валиден и не отозван
     */
    boolean isTokenValid(String token, User user);

    /**
     * Проверяет, отозван ли токен.
     *
     * @param token JWT токен
     * @return true, если токен находится в списке отозванных
     */
    boolean isTokenRevoked(String token);

    /**
     * Добавляет токен в список отозванных.
     * Не отзывает уже истекшие токены.
     *
     * @param token JWT токен для отзыва
     */
    void revokeToken(String token);
}