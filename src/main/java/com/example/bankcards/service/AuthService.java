package com.example.bankcards.service;

import com.example.bankcards.dto.UserLoginRequest;
import com.example.bankcards.dto.UserLoginResponse;
import com.example.bankcards.dto.UserRegisterRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.auth.InvalidPasswordException;
import com.example.bankcards.exception.auth.InvalidTokenException;
import com.example.bankcards.exception.users.UserExistsException;
import com.example.bankcards.exception.users.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

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

    public UserLoginResponse login(UserLoginRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(UserNotFoundException::new);
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException();
        }
        return jwtService.generateTokenPair(user);
    }

    public void logout(String refreshToken) {
        userRepository.findByRefreshToken(refreshToken)
                .ifPresent(user -> {
                    String accessToken = jwtService.extractAccessTokenFromRequest();
                    if (accessToken != null) jwtService.revokeToken(accessToken);
                    user.setRefreshToken(null);
                    userRepository.save(user);
                });
    }

    public UserLoginResponse refreshToken(String refreshToken) {

        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new InvalidTokenException("Refresh token is empty!");
        }

        if (jwtService.isTokenRevoked(refreshToken)) {
            throw new InvalidTokenException("Token revoked");
        }

        String email = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new InvalidTokenException("Discrepancy of refresh token!");
        }

        if (!jwtService.isTokenValid(refreshToken, user)) {
            if (user.getRefreshTokenExpiry() != null &&
                    user.getRefreshTokenExpiry().isBefore(LocalDateTime.now())) {
                throw new InvalidTokenException("The refresh token is expired!");
            }
            throw new InvalidTokenException("Refresh token is invalid!");
        }
        UserLoginResponse tokens = jwtService.generateTokenPair(user);
        jwtService.revokeToken(refreshToken);
        return tokens;
    }

}
