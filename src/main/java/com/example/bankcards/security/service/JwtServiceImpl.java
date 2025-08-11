package com.example.bankcards.security.service;

import com.example.bankcards.config.JwtConfig;
import com.example.bankcards.dto.users.UserLoginResponse;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.factory.JwtTokenFactory;
import com.example.bankcards.util.Mapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Реализация {@link JwtService} для работы с JWT токенами.
 * Генерирует пару токенов через {@link JwtTokenFactory} и сохраняет refresh токен в БД.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Getter
public class JwtServiceImpl implements JwtService {
    private final UserRepository userRepository;
    private final JwtConfig jwtConfig;
    private final Mapper mapper;
    private final JwtTokenFactory jwtTokenFactory;

    /**
     * {@inheritDoc}
     * <p>
     * Для администраторов добавляет authority ROLE_ADMIN, для обычных пользователей - ROLE_USER.
     * Сохраняет refresh токен и время его истечения в базу данных.
     */
    @Override
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

    /**
     * {@inheritDoc}
     * <p>
     * Логирует предупреждение, если контекст запроса недоступен.
     */
    @Override
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