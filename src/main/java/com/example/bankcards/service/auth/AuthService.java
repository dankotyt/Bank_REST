package com.example.bankcards.service.auth;

import com.example.bankcards.dto.users.UserLoginRequest;
import com.example.bankcards.dto.users.UserLoginResponse;
import com.example.bankcards.dto.users.UserRegisterRequest;

public interface AuthService {
    UserLoginResponse register(UserRegisterRequest userRegisterRequest);
    UserLoginResponse login(UserLoginRequest request);
    void logout(String refreshToken);
    UserLoginResponse refreshToken(String refreshToken);
}
