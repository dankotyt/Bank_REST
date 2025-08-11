package com.example.bankcards.security.service;

import com.example.bankcards.dto.users.UserLoginResponse;
import com.example.bankcards.entity.User;

/**
 * Сервис для работы с JWT токенами.
 * Предоставляет методы для генерации пары токенов (access + refresh) и извлечения токена из запроса.
 */
public interface JwtService {
    /**
     * Генерирует пару JWT токенов (access + refresh) для пользователя.
     *
     * @param user пользователь, для которого генерируются токены
     * @return объект UserLoginResponse с токенами и информацией о пользователе
     */
    UserLoginResponse generateTokenPair(User user);

    /**
     * Извлекает access токен из HTTP запроса.
     * Ищет токен в заголовке Authorization (формат "Bearer {token}").
     *
     * @return access токен или null, если токен не найден или контекст запроса недоступен
     */
    String extractAccessTokenFromRequest();
}
