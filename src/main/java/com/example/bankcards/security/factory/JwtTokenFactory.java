package com.example.bankcards.security.factory;

import com.example.bankcards.entity.User;

import java.util.List;

/**
 * Фабрика для создания JWT токенов (access и refresh).
 * Предоставляет методы для генерации подписанных токенов с указанными claims.
 */
public interface JwtTokenFactory {

    /**
     * Создает access токен для пользователя с указанными authorities.
     *
     * @param user пользователь, для которого создается токен
     * @param authorities список прав пользователя
     * @return подписанный JWT access токен
     */
    String createAccessToken(User user, List<String> authorities);

    /**
     * Создает refresh токен для указанного пользователя.
     *
     * @param user пользователь, для которого создается токен
     * @return подписанный JWT refresh токен
     */
    String createRefreshToken(User user);
}
