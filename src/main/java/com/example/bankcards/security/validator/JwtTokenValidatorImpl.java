package com.example.bankcards.security.validator;

import com.example.bankcards.entity.User;
import com.example.bankcards.security.parser.JwtParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenValidatorImpl implements JwtTokenValidator {
    private final Set<String> revokedTokens = ConcurrentHashMap.newKeySet();
    private final JwtParser jwtParser;

    @Override
    public void revokeToken(String token) {
        if (!jwtParser.isTokenExpired(token)) {
            revokedTokens.add(token);
        }
    }

    @Override
    public boolean isTokenRevoked(String token) {
        return revokedTokens.contains(token);
    }

    @Override
    public boolean isTokenValid(String token, User user) {
        try {
            final String username = jwtParser.extractUsername(token);
            return username.equals(user.getEmail())
                    && !revokedTokens.contains(token)
                    && !jwtParser.isTokenExpired(token);
        } catch (Exception e) {
            log.error("Token validation failed", e);
            return false;
        }
    }
}
