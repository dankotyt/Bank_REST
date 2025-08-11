package com.example.bankcards.security.factory;

import com.example.bankcards.entity.User;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * Реализация {@link JwtTokenFactory} для создания JWT токенов с использованием JJWT.
 * Генерирует токены с указанным временем жизни и подписывает их секретным ключом.
 */
@RequiredArgsConstructor
public class JwtTokenFactoryImpl implements JwtTokenFactory {
    private final SecretKey secretKey;
    private final long accessTtl;
    private final long refreshTtl;

    /**
     * {@inheritDoc}
     * <p>
     * Access токен содержит:
     * - subject (email пользователя)
     * - issuedAt (время создания)
     * - expiration (время истечения)
     * - claim "authorities" (список прав)
     */
    @Override
    public String createAccessToken(User user, List<String> authorities) {
        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusMillis(accessTtl)))
                .claim("authorities", authorities)
                .signWith(secretKey)
                .compact();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Refresh токен содержит:
     * - subject (email пользователя)
     * - issuedAt (время создания)
     * - expiration (время истечения)
     */
    @Override
    public String createRefreshToken(User user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(refreshTtl)))
                .signWith(secretKey)
                .compact();
    }
}
