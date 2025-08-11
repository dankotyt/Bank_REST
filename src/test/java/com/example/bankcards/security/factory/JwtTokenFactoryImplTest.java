package com.example.bankcards.security.factory;

import com.example.bankcards.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class JwtTokenFactoryImplTest {

    private JwtTokenFactory jwtTokenFactory;
    private SecretKey secretKey;
    private User testUser;

    @BeforeEach
    void setUp() {
        String secretString = Base64.getEncoder().encodeToString("test-secret-key-1234567890".getBytes());
        secretKey = new SecretKeySpec(secretString.getBytes(), "HmacSHA256");

        long refreshTtl = 86400000;
        long accessTtl = 3600000;
        jwtTokenFactory = new JwtTokenFactoryImpl(secretKey, accessTtl, refreshTtl);

        testUser = User.builder()
                .userId(1L)
                .email("test@example.com")
                .build();
    }

    @Test
    void createAccessToken_ShouldReturnValidToken() {
        List<String> authorities = List.of("ROLE_USER");

        String token = jwtTokenFactory.createAccessToken(testUser, authorities);

        assertThat(token).isNotBlank();

        Jws<Claims> claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);

        assertThat(claims.getPayload().getSubject()).isEqualTo(testUser.getEmail());
        assertThat(claims.getPayload().get("authorities")).isEqualTo(authorities);
    }

    @Test
    void createRefreshToken_ShouldReturnValidToken() {
        String token = jwtTokenFactory.createRefreshToken(testUser);

        assertThat(token).isNotBlank();

        Jws<Claims> claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);

        assertThat(claims.getPayload().getSubject()).isEqualTo(testUser.getEmail());
        assertThat(claims.getPayload().get("authorities")).isNull();
    }
}