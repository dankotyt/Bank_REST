package com.example.bankcards.service;

import com.example.bankcards.dto.UserLoginResponse;
import com.example.bankcards.dto.UserRegisterRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserExistsException;
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


}
