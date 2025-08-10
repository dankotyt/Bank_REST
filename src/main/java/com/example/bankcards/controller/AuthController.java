package com.example.bankcards.controller;

import com.example.bankcards.dto.users.UserLoginRequest;
import com.example.bankcards.dto.users.UserLoginResponse;
import com.example.bankcards.dto.users.UserRegisterRequest;
import com.example.bankcards.exception.auth.InvalidTokenException;
import com.example.bankcards.security.cookie.CookieServiceImpl;
import com.example.bankcards.service.auth.AuthServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthServiceImpl authServiceImpl;
    private final CookieServiceImpl cookieServiceImpl;

    @PostMapping("/register")
    public ResponseEntity<UserLoginResponse> registerUser(
            @RequestBody UserRegisterRequest request,
            HttpServletResponse response) {

        UserLoginResponse loginResponse = authServiceImpl.register(request);
        cookieServiceImpl.setRefreshTokenCookie(response, loginResponse.getRefreshToken());
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> loginUser(
            @RequestBody UserLoginRequest request,
            HttpServletResponse response) {
        UserLoginResponse loginResponse = authServiceImpl.login(request);
        cookieServiceImpl.setRefreshTokenCookie(response, loginResponse.getRefreshToken());
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<UserLoginResponse> refresh(
            @CookieValue(value = "__Host-refresh", required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken == null) {
            cookieServiceImpl.expireAllCookies(response);
            throw new InvalidTokenException("Refresh token required");
        }
        try {
            UserLoginResponse tokens = authServiceImpl.refreshToken(refreshToken);

            cookieServiceImpl.setAccessTokenCookie(response, tokens.getAccessToken());
            cookieServiceImpl.setRefreshTokenCookie(response, tokens.getRefreshToken());

            return ResponseEntity.ok(tokens);
        } catch (InvalidTokenException e) {
            cookieServiceImpl.expireAllCookies(response);
            throw e;
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue("__Host-refresh") String refreshToken,
            HttpServletResponse response) {

        authServiceImpl.logout(refreshToken);
        cookieServiceImpl.expireAllCookies(response);
        return ResponseEntity.ok().build();
    }
}
