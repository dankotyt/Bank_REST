package com.example.bankcards.security.cookie;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Сервис для работы с HTTP-куками аутентификации.
 * Предоставляет методы для установки и удаления токенов доступа и обновления.
 */
public interface CookieService {

    /**
     * Устанавливает куку с access-токеном.
     *
     * @param response HTTP-ответ, в который будет добавлена кука
     * @param accessToken JWT access-токен для сохранения в куке
     */
    void setAccessTokenCookie(HttpServletResponse response, String accessToken);

    /**
     * Устанавливает куку с refresh-токеном.
     *
     * @param response HTTP-ответ, в который будет добавлена кука
     * @param refreshToken JWT refresh-токен для сохранения в куке
     */
    void setRefreshTokenCookie(HttpServletResponse response, String refreshToken);

    /**
     * Удаляет все куки аутентификации (access и refresh токены).
     *
     * @param response HTTP-ответ, в который будут добавлены куки с истекшим сроком
     */
    void expireAllCookies(HttpServletResponse response);

    /**
     * Удаляет конкретную куку по имени.
     *
     * @param response HTTP-ответ, в который будет добавлена кука с истекшим сроком
     * @param name имя куки для удаления
     */
    void expireCookie(HttpServletResponse response, String name);
}
