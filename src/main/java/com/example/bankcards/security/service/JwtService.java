package com.example.bankcards.security.service;

import com.example.bankcards.dto.users.UserLoginResponse;
import com.example.bankcards.entity.User;

public interface JwtService {

    UserLoginResponse generateTokenPair(User user);
    String extractAccessTokenFromRequest();
}
