package com.example.bankcards.security.parser;

import com.example.bankcards.exception.auth.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

/**
 * Реализация {@link JwtParser} для работы с JWT токенами с использованием JJWT.
 * Проверяет подпись токена и извлекает claims.
 */
@Component
@RequiredArgsConstructor
public class JwtParserImpl implements JwtParser {
    private final SecretKey secretKey;

    /**
     * {@inheritDoc}
     * <p>
     * Проверяет подпись токена с использованием секретного ключа.
     */
    @Override
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw new InvalidTokenException("Incorrect JWT token: " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Сначала парсит токен, затем применяет функцию к claims.
     */
    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = parseToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Извлекает subject из claims токена.
     */
    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Извлекает expiration из claims токена.
     */
    @Override
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Сравнивает дату истечения с текущей датой.
     */
    @Override
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
