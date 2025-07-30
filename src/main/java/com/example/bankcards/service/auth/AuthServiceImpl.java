package com.example.bankcards.service.auth;

import com.example.bankcards.dto.users.UserLoginRequest;
import com.example.bankcards.dto.users.UserLoginResponse;
import com.example.bankcards.dto.users.UserRegisterRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.auth.InvalidPasswordException;
import com.example.bankcards.exception.auth.InvalidTokenException;
import com.example.bankcards.exception.users.UserExistsException;
import com.example.bankcards.exception.users.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.parser.JwtParser;
import com.example.bankcards.security.service.JwtService;
import com.example.bankcards.security.service.JwtServiceImpl;
import com.example.bankcards.security.validator.JwtTokenValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Реализация сервиса аутентификации и авторизации.
 * <p>
 * Особенности реализации:
 * <ul>
 *   <li>Использует {@link PasswordEncoder} для безопасного хранения паролей</li>
 *   <li>Генерирует JWT токены через {@link JwtServiceImpl}</li>
 *   <li>Автоматически устанавливает дату создания при регистрации</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtParser jwtParser;
    private final JwtTokenValidator jwtTokenValidator;

    /**
     * {@inheritDoc}
     * <p>Дополнительно проверяет уникальность email и номера телефона.
     * Пароль хешируется перед сохранением.
     */
    @Override
    public UserLoginResponse register(UserRegisterRequest userRegisterRequest) {
        if (userRepository.existsByEmailOrPhoneNumber(userRegisterRequest.getEmail(), userRegisterRequest.getPhoneNumber())) {
            throw new UserExistsException("User with this email or phone number already exists!");
        }
        var user = User.builder()
                .name(userRegisterRequest.getName())
                .surname(userRegisterRequest.getSurname())
                .patronymic(userRegisterRequest.getPatronymic())
                .birthday(userRegisterRequest.getBirthday())
                .role(userRegisterRequest.getRole())
                .email(userRegisterRequest.getEmail())
                .phoneNumber(String.valueOf(userRegisterRequest.getPhoneNumber()))
                .password(passwordEncoder.encode(userRegisterRequest.getPassword()))
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(user);
        return jwtService.generateTokenPair(user);
    }

    /**
     * {@inheritDoc}
     * <p>Сравнивает хеш пароля из базы с хешем введенного пароля.
     */
    @Override
    public UserLoginResponse login(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(UserNotFoundException::new);
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException();
        }
        return jwtService.generateTokenPair(user);
    }

    /**
     * {@inheritDoc}
     * <p>Отзывает оба токена (access и refresh) при выходе.
     */
    @Override
    public void logout(String refreshToken) {
        userRepository.findByRefreshToken(refreshToken)
                .ifPresent(user -> {
                    String accessToken = jwtService.extractAccessTokenFromRequest();
                    if (accessToken != null) jwtTokenValidator.revokeToken(accessToken);
                    user.setRefreshToken(null);
                    userRepository.save(user);
                });
    }

    /**
     * {@inheritDoc}
     * <p>Перед генерацией новых токенов выполняет расширенную проверку:
     * <ul>
     *   <li>Сравнение токена с сохраненным в БД</li>
     *   <li>Проверку срока действия</li>
     *   <li>Проверку отзыва токена</li>
     * </ul>
     */
    @Override
    public UserLoginResponse refreshToken(String refreshToken) {

        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new InvalidTokenException("Refresh token is empty!");
        }

        if (jwtTokenValidator.isTokenRevoked(refreshToken)) {
            throw new InvalidTokenException("Token revoked");
        }

        String email = jwtParser.extractUsername(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new InvalidTokenException("Discrepancy of refresh token!");
        }

        if (!jwtTokenValidator.isTokenValid(refreshToken, user)) {
            if (user.getRefreshTokenExpiry() != null &&
                    user.getRefreshTokenExpiry().isBefore(LocalDateTime.now())) {
                throw new InvalidTokenException("The refresh token is expired!");
            }
            throw new InvalidTokenException("Refresh token is invalid!");
        }
        UserLoginResponse tokens = jwtService.generateTokenPair(user);
        jwtTokenValidator.revokeToken(refreshToken);
        return tokens;
    }
}
