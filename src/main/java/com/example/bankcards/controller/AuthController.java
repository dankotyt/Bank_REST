package com.example.bankcards.controller;

import com.example.bankcards.dto.users.UserLoginRequest;
import com.example.bankcards.dto.users.UserLoginResponse;
import com.example.bankcards.dto.users.UserRegisterRequest;
import com.example.bankcards.exception.auth.InvalidTokenException;
import com.example.bankcards.security.CookieService;
import com.example.bankcards.service.AuthService;
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
    private final AuthService authService;
    private final CookieService cookieService;

    @PostMapping("/register")
    public ResponseEntity<UserLoginResponse> registerUser(
            @RequestBody UserRegisterRequest request,
            HttpServletResponse response) {

        UserLoginResponse loginResponse = authService.register(request);
        cookieService.setRefreshTokenCookie(response, loginResponse.getRefreshToken());
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> loginUser(
            @RequestBody UserLoginRequest request,
            HttpServletResponse response) {
        UserLoginResponse loginResponse = authService.login(request);
        cookieService.setRefreshTokenCookie(response, loginResponse.getRefreshToken());
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<UserLoginResponse> refresh(
            @CookieValue(value = "__Host-refresh", required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken == null) {
            throw new InvalidTokenException("Refresh token required");
        }
        try {
            UserLoginResponse tokens = authService.refreshToken(refreshToken);

            cookieService.setAccessTokenCookie(response, tokens.getAccessToken());
            cookieService.setRefreshTokenCookie(response, tokens.getRefreshToken());

            return ResponseEntity.ok(tokens);
        } catch (InvalidTokenException e) {
            cookieService.expireAllCookies(response);
            throw e;
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue("__Host-refresh") String refreshToken,
            HttpServletResponse response) {

        authService.logout(refreshToken);
        cookieService.expireAllCookies(response);
        return ResponseEntity.ok().build();
    }
}
