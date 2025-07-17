package com.example.bankcards.security;

import com.example.bankcards.config.JwtConfig;
import com.example.bankcards.dto.UserLoginResponse;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.InvalidTokenException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.UserMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.crypto.SecretKey;
import org.springframework.http.HttpHeaders;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
@Slf4j
@Getter
public class JwtService {
    private final UserRepository userRepository;
    private final JwtConfig jwtConfig;
    private final SecretKey secretKey;
    private final UserMapper userMapper;
    private final Set<String> revokedTokens = ConcurrentHashMap.newKeySet();
    @Value("${jwt.refresh-ttl}")
    private long refreshTtl;
    @Value("${jwt.access-ttl}")
    private long accessTtl;

    //TOKEN FACTORY========
    private String createAccessToken(User user, List<String> authorities) {
        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusMillis(accessTtl)))
                .claim("authorities", authorities)
                .signWith(secretKey)
                .compact();
    }

    private String createRefreshToken(User user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusMillis(refreshTtl)))
                .signWith(secretKey)
                .compact();
    }
    //=====================

    public UserLoginResponse generateTokenPair(User user) {
        List<String> authorities = user.getRole() == Role.ADMIN
                ? List.of("ADMIN")
                : List.of("USER");
        String accessToken = createAccessToken(user, authorities);
        String refreshToken = createRefreshToken(user);

        LocalDateTime expiry = LocalDateTime.now().plusSeconds(refreshTtl / 1000);
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(expiry);
        userRepository.save(user);

        return new UserLoginResponse(
                accessToken,
                refreshToken,
                userMapper.toDTO(user)
        );
    }

    //TOKEN PARSER========
    private Claims parseToken(String token) {
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

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = parseToken(token);
        return claimsResolver.apply(claims);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    //=====================
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public void revokeToken(String token) {
        if (!isTokenExpired(token)) {
            revokedTokens.add(token);
        }
    }

    public boolean isTokenRevoked(String token) {
        return revokedTokens.contains(token);
    }

    public boolean isTokenValid(String token, User user) {
        try {
            final String username = extractUsername(token);
            return username.equals(user.getEmail())
                    && !revokedTokens.contains(token)
                    && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Token validation failed", e);
            return false;
        }
    }

    public String extractAccessTokenFromRequest() {
        try {
            ServletRequestAttributes requestAttributes =
                    (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = requestAttributes.getRequest();

            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }

            return null;
        } catch (IllegalStateException e) {
            log.warn("Request context not available");
            return null;
        }
    }

    @Bean
    public long accessTtl() {
        return accessTtl;
    }

    @Bean
    public long refreshTtl() {
        return refreshTtl;
    }
}
