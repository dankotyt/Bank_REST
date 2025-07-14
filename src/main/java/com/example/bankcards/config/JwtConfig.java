package com.example.bankcards.config;

import com.example.bankcards.repository.UserRepository;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.crypto.SecretKey;
import java.util.Base64;

@Configuration
@Getter
@Setter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    private String secret;

    @Bean
    @Primary
    public SecretKey secretKey() {
        byte[] decodedKey = Base64.getDecoder().decode(secret.trim());
        return Keys.hmacShaKeyFor(decodedKey);
    }
}
