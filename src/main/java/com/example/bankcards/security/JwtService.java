package com.example.bankcards.security;

import com.example.bankcards.config.JwtConfig;
import com.example.bankcards.dto.users.UserLoginResponse;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.auth.InvalidTokenException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.Mapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
@Getter
public class JwtService {
    private final UserRepository userRepository;
    private final JwtConfig jwtConfig;
    private final Mapper mapper;
    private final Set<String> revokedTokens = ConcurrentHashMap.newKeySet();
    private final JwtTokenFactory jwtTokenFactory;
    private final SecretKey secretKey;

    //=====================

    public UserLoginResponse generateTokenPair(User user) {
        List<String> authorities = user.getRole() == Role.ADMIN
                ? List.of("ROLE_ADMIN")
                : List.of("ROLE_USER");
        String accessToken = jwtTokenFactory.createAccessToken(user, authorities);
        String refreshToken = jwtTokenFactory.createRefreshToken(user);

        LocalDateTime expiry = LocalDateTime.now().plusSeconds(jwtConfig.getRefreshTtl() / 1000);
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(expiry);
        userRepository.save(user);

        return new UserLoginResponse(
                accessToken,
                refreshToken,
                mapper.toDTO(user)
        );
    }


    //TOKEN PARSER========
    protected Claims parseToken(String token) {
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

    protected  <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = parseToken(token);
        return claimsResolver.apply(claims);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    //=====================
    protected Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    protected boolean isTokenExpired(String token) {
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
}
