package com.example.bankcards.controller;

import com.example.bankcards.dto.users.UserLoginRequest;
import com.example.bankcards.dto.users.UserLoginResponse;
import com.example.bankcards.dto.users.UserRegisterRequest;
import com.example.bankcards.exception.auth.InvalidTokenException;
import com.example.bankcards.security.CookieService;
import com.example.bankcards.service.auth.AuthServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication API", description = "Аутентификация и управление сессиями")
public class AuthController {
    private final AuthServiceImpl authServiceImpl;
    private final CookieService cookieService;

    @Operation(summary = "Регистрация", description = "Регистрирует нового пользователя в системе")
    @ApiResponse(responseCode = "200", description = "Успешная регистрация",
            content = @Content(schema = @Schema(implementation = UserLoginResponse.class)))
    @ApiResponse(responseCode = "400", description = "Некорректные данные")
    @PostMapping("/register")
    public ResponseEntity<UserLoginResponse> registerUser(
            @RequestBody UserRegisterRequest request,
            HttpServletResponse response) {

        UserLoginResponse loginResponse = authServiceImpl.register(request);
        cookieService.setRefreshTokenCookie(response, loginResponse.getRefreshToken());
        return ResponseEntity.ok(loginResponse);
    }

    @Operation(summary = "Вход в систему", description = "Аутентифицирует пользователя и возвращает токены")
    @ApiResponse(responseCode = "200", description = "Успешный вход",
            content = @Content(schema = @Schema(implementation = UserLoginResponse.class)))
    @ApiResponse(responseCode = "401", description = "Неверные учетные данные")
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> loginUser(
            @RequestBody UserLoginRequest request,
            HttpServletResponse response) {
        UserLoginResponse loginResponse = authServiceImpl.login(request);
        cookieService.setRefreshTokenCookie(response, loginResponse.getRefreshToken());
        return ResponseEntity.ok(loginResponse);
    }

    @Operation(summary = "Обновление токена", description = "Обновляет access token по refresh token")
    @ApiResponse(responseCode = "200", description = "Токены обновлены",
            content = @Content(schema = @Schema(implementation = UserLoginResponse.class)))
    @ApiResponse(responseCode = "401", description = "Недействительный refresh token")
    @PostMapping("/refresh")
    public ResponseEntity<UserLoginResponse> refresh(
            @CookieValue(value = "__Host-refresh", required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken == null) {
            cookieService.expireAllCookies(response);
            throw new InvalidTokenException("Refresh token required");
        }
        try {
            UserLoginResponse tokens = authServiceImpl.refreshToken(refreshToken);

            cookieService.setAccessTokenCookie(response, tokens.getAccessToken());
            cookieService.setRefreshTokenCookie(response, tokens.getRefreshToken());

            return ResponseEntity.ok(tokens);
        } catch (InvalidTokenException e) {
            cookieService.expireAllCookies(response);
            throw e;
        }
    }

    @Operation(summary = "Выход из системы", description = "Завершает сеанс пользователя")
    @ApiResponse(responseCode = "200", description = "Успешный выход")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Parameter(description = "Refresh token из cookies", required = true)
            @CookieValue("__Host-refresh") String refreshToken,
            HttpServletResponse response) {

        authServiceImpl.logout(refreshToken);
        cookieService.expireAllCookies(response);
        return ResponseEntity.ok().build();
    }
}
