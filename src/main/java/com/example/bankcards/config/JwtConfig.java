package com.example.bankcards.config;

import com.example.bankcards.security.factory.JwtTokenFactoryImpl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@Getter
@Setter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "jwt")
@Slf4j
public class JwtConfig {
    private String secret;
    private long accessTtl;
    private long refreshTtl;

    @Bean
    public JwtTokenFactoryImpl jwtTokenFactory(SecretKey secretKey) {
        return new JwtTokenFactoryImpl(secretKey, accessTtl, refreshTtl);
    }

    @Bean
    public SecretKey secretKey() {
        try {
            log.info("Raw JWT secret: {}", secret);
            byte[] decodedKey = Base64.getDecoder().decode(secret.trim());
            log.info("Decoded key length: {}", decodedKey.length);
            return new SecretKeySpec(decodedKey, "HmacSHA256");
        } catch (IllegalArgumentException e) {
            log.error("Invalid JWT secret", e);
            throw new IllegalArgumentException("Invalid Base64 JWT secret", e);
        }
    }
}
