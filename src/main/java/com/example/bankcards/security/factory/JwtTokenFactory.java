package com.example.bankcards.security.factory;

import com.example.bankcards.entity.User;

import java.util.List;

public interface JwtTokenFactory {
    String createAccessToken(User user, List<String> authorities);
    String createRefreshToken(User user);
}
