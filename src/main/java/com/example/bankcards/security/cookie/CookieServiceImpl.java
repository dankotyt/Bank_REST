package com.example.bankcards.security.cookie;

import com.example.bankcards.config.JwtConfig;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

/**
 * Реализация {@link CookieService} для работы с куками аутентификации.
 * Использует безопасные настройки кук (HttpOnly, Secure, SameSite Strict).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CookieServiceImpl implements CookieService {
    private final JwtConfig jwtConfig;

    /**
     * {@inheritDoc}
     * <p>
     * Устанавливает access-токен в куку с параметрами:
     * - Имя: "__Host-auth-token"
     * - HttpOnly: true
     * - Secure: true
     * - Путь: "/"
     * - SameSite: Strict
     * - Срок жизни: из настроек JWT (в секундах)
     */
    @Override
    public void setAccessTokenCookie(HttpServletResponse response, String accessToken) {
        ResponseCookie cookie = ResponseCookie.from("__Host-auth-token", accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge((int) jwtConfig.getAccessTtl() / 1000)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Устанавливает refresh-токен в куку с параметрами:
     * - Имя: "__Host-refresh"
     * - HttpOnly: true
     * - Secure: true
     * - Путь: "/"
     * - SameSite: Strict
     * - Срок жизни: из настроек JWT (в секундах)
     * <p>
     * Логирует установку refresh-токена.
     */
    @Override
    public void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        log.info("Setting refresh token cookie: {}", refreshToken);
        ResponseCookie cookie = ResponseCookie.from("__Host-refresh", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge((int) jwtConfig.getRefreshTtl() / 1000)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Удаляет обе куки аутентификации (access и refresh токены).
     */
    @Override
    public void expireAllCookies(HttpServletResponse response) {
        expireCookie(response, "__Host-auth-token");
        expireCookie(response, "__Host-refresh");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Удаляет куку по имени, устанавливая maxAge=0.
     */
    @Override
    public void expireCookie(HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .maxAge(0)
                .path("/")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
