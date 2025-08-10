package com.example.bankcards.security.cookie;

import jakarta.servlet.http.HttpServletResponse;

public interface CookieService {
    void setAccessTokenCookie(HttpServletResponse response, String accessToken);
    void setRefreshTokenCookie(HttpServletResponse response, String refreshToken);
    void expireAllCookies(HttpServletResponse response);
    void expireCookie(HttpServletResponse response, String name);
}
