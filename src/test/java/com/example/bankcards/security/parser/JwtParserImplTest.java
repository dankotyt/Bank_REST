package com.example.bankcards.security.parser;

import com.example.bankcards.exception.auth.InvalidTokenException;
import io.jsonwebtoken.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

import io.jsonwebtoken.security.Keys;

@ExtendWith(MockitoExtension.class)
class JwtParserImplTest {

    private JwtParserImpl jwtParser;
    private SecretKey testKey;
    private final String testSubject = "test@example.com";

    @BeforeEach
    void setUp() {
        testKey = Keys.hmacShaKeyFor("test-secret-key-1234567890-1234567890".getBytes());
        jwtParser = new JwtParserImpl(testKey);
    }

    @Test
    void parseToken_WithValidToken_ShouldReturnClaims() {
        String token = Jwts.builder()
                .subject(testSubject)
                .expiration(new Date(System.currentTimeMillis() + 10000))
                .signWith(testKey)
                .compact();

        Claims claims = jwtParser.parseToken(token);

        assertNotNull(claims);
        assertEquals(testSubject, claims.getSubject());
    }

    @Test
    void parseToken_WithInvalidToken_ShouldThrowInvalidTokenException() {
        String invalidToken = "invalid.token";

        assertThrows(InvalidTokenException.class, () -> jwtParser.parseToken(invalidToken));
    }

    @Test
    void parseToken_WithExpiredToken_ShouldThrowException() {
        String token = Jwts.builder()
                .subject(testSubject)
                .expiration(new Date(System.currentTimeMillis() - 10000))
                .signWith(testKey)
                .compact();

        assertThrows(InvalidTokenException.class, () -> jwtParser.parseToken(token));
    }

    @Test
    void extractClaim_ShouldReturnCorrectValue() {
        String token = Jwts.builder()
                .subject(testSubject)
                .expiration(new Date(System.currentTimeMillis() + 10000))
                .signWith(testKey)
                .compact();

        String subject = jwtParser.extractClaim(token, Claims::getSubject);

        assertEquals(testSubject, subject);
    }

    @Test
    void extractUsername_ShouldReturnSubject() {
        String token = Jwts.builder()
                .subject(testSubject)
                .expiration(new Date(System.currentTimeMillis() + 10000))
                .signWith(testKey)
                .compact();

        String username = jwtParser.extractUsername(token);

        assertEquals(testSubject, username);
    }

    @Test
    void extractExpiration_ShouldReturnCorrectDate() {
        long expectedTime = System.currentTimeMillis() + 10000;
        String token = Jwts.builder()
                .subject(testSubject)
                .expiration(new Date(expectedTime))
                .signWith(testKey)
                .compact();

        Date expiration = jwtParser.extractExpiration(token);

        assertEquals(expectedTime, expiration.getTime(), 1000);
    }

    @Test
    void isTokenExpired_WithValidToken_ShouldReturnFalse() {
        String token = Jwts.builder()
                .subject(testSubject)
                .expiration(new Date(System.currentTimeMillis() + 10000))
                .signWith(testKey)
                .compact();

        boolean isExpired = jwtParser.isTokenExpired(token);

        assertFalse(isExpired);
    }
}