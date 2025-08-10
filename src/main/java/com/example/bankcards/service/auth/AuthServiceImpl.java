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
import com.example.bankcards.security.validator.JwtTokenValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtParser jwtParser;
    private final JwtTokenValidator jwtTokenValidator;

    @Override
    public UserLoginResponse register(UserRegisterRequest userRegisterRequest) {
        if (userRepository.existsByEmail(userRegisterRequest.getEmail())) {
            throw new UserExistsException("User with this email or phone number already exists!");
        }
        var user = User.builder()
                .name(userRegisterRequest.getName())
                .surname(userRegisterRequest.getSurname())
                .patronymic(userRegisterRequest.getPatronymic())
                .birthday(userRegisterRequest.getBirthday())
                .role(userRegisterRequest.getRole())
                .email(userRegisterRequest.getEmail())
                .password(passwordEncoder.encode(userRegisterRequest.getPassword()))
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(user);
        return jwtService.generateTokenPair(user);
    }

    @Override
    public UserLoginResponse login(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(UserNotFoundException::new);
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException();
        }
        return jwtService.generateTokenPair(user);
    }

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
