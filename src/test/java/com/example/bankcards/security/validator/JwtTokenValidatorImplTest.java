package com.example.bankcards.security.validator;

import com.example.bankcards.entity.User;
import com.example.bankcards.security.parser.JwtParser;
import com.example.bankcards.security.validator.JwtTokenValidatorImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenValidatorImplTest {

    @Mock
    private JwtParser jwtParser;

    @InjectMocks
    private JwtTokenValidatorImpl jwtTokenValidator;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .build();
    }

    @Test
    void revokeToken_WhenTokenNotExpired_ShouldAddToRevokedTokens() {
        String token = "valid.token";
        when(jwtParser.isTokenExpired(token)).thenReturn(false);

        jwtTokenValidator.revokeToken(token);

        assertTrue(jwtTokenValidator.isTokenRevoked(token));
    }

    @Test
    void revokeToken_WhenTokenExpired_ShouldNotAddToRevokedTokens() {
        String token = "expired.token";
        when(jwtParser.isTokenExpired(token)).thenReturn(true);

        jwtTokenValidator.revokeToken(token);

        assertFalse(jwtTokenValidator.isTokenRevoked(token));
    }

    @Test
    void isTokenValid_WhenAllConditionsMet_ShouldReturnTrue() {
        String token = "valid.token";
        when(jwtParser.extractUsername(token)).thenReturn("test@example.com");
        when(jwtParser.isTokenExpired(token)).thenReturn(false);

        boolean isValid = jwtTokenValidator.isTokenValid(token, testUser);

        assertTrue(isValid);
    }

    @Test
    void isTokenValid_WhenUsernameMismatch_ShouldReturnFalse() {
        String token = "valid.token";
        when(jwtParser.extractUsername(token)).thenReturn("other@example.com");

        boolean isValid = jwtTokenValidator.isTokenValid(token, testUser);

        assertFalse(isValid);
    }

    @Test
    void isTokenValid_WhenTokenRevoked_ShouldReturnFalse() {
        String token = "revoked.token";
        jwtTokenValidator.revokeToken(token);

        lenient().when(jwtParser.extractUsername(token)).thenReturn("test@example.com");
        lenient().when(jwtParser.isTokenExpired(token)).thenReturn(false);

        boolean isValid = jwtTokenValidator.isTokenValid(token, testUser);

        assertFalse(isValid);
    }

    @Test
    void isTokenValid_WhenTokenExpired_ShouldReturnFalse() {
        String token = "expired.token";
        when(jwtParser.extractUsername(token)).thenReturn("test@example.com");
        when(jwtParser.isTokenExpired(token)).thenReturn(true);

        boolean isValid = jwtTokenValidator.isTokenValid(token, testUser);

        assertFalse(isValid);
    }

    @Test
    void isTokenValid_WhenTokenParsingFails_ShouldReturnFalse() {
        String token = "invalid.token";
        when(jwtParser.extractUsername(token)).thenThrow(new RuntimeException("Parsing failed"));

        boolean isValid = jwtTokenValidator.isTokenValid(token, testUser);

        assertFalse(isValid);
    }
}