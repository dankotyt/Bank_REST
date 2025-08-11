package com.example.bankcards.security.parser;

import com.example.bankcards.exception.auth.InvalidTokenException;
import io.jsonwebtoken.Claims;

import java.util.Date;
import java.util.function.Function;

/**
 * Парсер JWT токенов.
 * Предоставляет методы для извлечения информации из токенов и проверки их валидности.
 */
public interface JwtParser {

    /**
     * Парсит JWT токен и возвращает его claims.
     *
     * @param token JWT токен
     * @return claims токена
     * @throws InvalidTokenException если токен невалиден
     */
    Claims parseToken(String token);

    /**
     * Извлекает конкретный claim из токена.
     *
     * @param token JWT токен
     * @param claimsResolver функция для извлечения claim
     * @param <T> тип возвращаемого значения
     * @return значение claim
     */
    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);

    /**
     * Извлекает имя пользователя (subject) из токена.
     *
     * @param token JWT токен
     * @return email пользователя
     */
    String extractUsername(String token);

    /**
     * Извлекает дату истечения токена.
     *
     * @param token JWT токен
     * @return дата истечения
     */
    Date extractExpiration(String token);

    /**
     * Проверяет, истек ли срок действия токена.
     *
     * @param token JWT токен
     * @return true, если токен истек
     */
    boolean isTokenExpired(String token);
}
