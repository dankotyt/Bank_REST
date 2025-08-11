package com.example.bankcards.controller;

import com.example.bankcards.dto.users.UserDTO;
import com.example.bankcards.dto.users.UserLoginRequest;
import com.example.bankcards.dto.users.UserLoginResponse;
import com.example.bankcards.dto.users.UserRegisterRequest;
import com.example.bankcards.entity.Role;
import com.example.bankcards.exception.GlobalExceptionHandler;
import com.example.bankcards.exception.auth.InvalidPasswordException;
import com.example.bankcards.exception.auth.InvalidTokenException;
import com.example.bankcards.exception.users.UserExistsException;
import com.example.bankcards.security.cookie.CookieServiceImpl;
import com.example.bankcards.service.auth.AuthServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthServiceImpl authServiceImpl;
    @Mock
    private CookieServiceImpl cookieServiceImpl;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(exceptionHandler)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void registerUser_ShouldReturnTokensAndUserDTO() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest(
                "John", "Doe", "Smith", LocalDate.of(1990, 1, 1),
                "john@example.com", "password123", Role.USER
        );
        UserLoginResponse mockResponse = new UserLoginResponse(
                "access", "refresh", new UserDTO()
        );

        when(authServiceImpl.register(any())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("access"))
                .andExpect(jsonPath("$.refresh_token").value("refresh"));

        verify(cookieServiceImpl).setRefreshTokenCookie(any(), eq("refresh"));
    }

    @Test
    void loginUser_ShouldReturnTokensAndSetCookie() throws Exception {
        UserLoginRequest request = new UserLoginRequest("john@example.com", "password123");
        UserLoginResponse mockResponse = new UserLoginResponse(
                "access-token", "refresh-token", testUserDTO
        );

        when(authServiceImpl.login(any(UserLoginRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("access-token"))
                .andExpect(jsonPath("$.refresh_token").value("refresh-token"))
                .andExpect(jsonPath("$.user").value(testUserDTO));

        verify(cookieServiceImpl).setRefreshTokenCookie(any(), eq("refresh-token"));
    }

    @Test
    void refresh_ShouldReturnNewTokensAndSetCookies() throws Exception {
        String validRefreshToken = "valid-refresh-token";
        UserLoginResponse mockResponse = new UserLoginResponse(
                "new-access", "new-refresh", testUserDTO
        );

        when(authServiceImpl.refreshToken(eq(validRefreshToken))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie("__Host-refresh", validRefreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("new-access"))
                .andExpect(jsonPath("$.refresh_token").value("new-refresh"));

        verify(cookieServiceImpl).setAccessTokenCookie(any(), eq("new-access"));
        verify(cookieServiceImpl).setRefreshTokenCookie(any(), eq("new-refresh"));
    }

    @Test
    void refresh_WhenNoToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Authentication Error"))
                .andExpect(jsonPath("$.message").value("Refresh token required"));
    }

    @Test
    void refresh_WhenInvalidToken_ShouldReturnUnauthorized() throws Exception {
        String invalidToken = "invalid-token";
        when(authServiceImpl.refreshToken(eq(invalidToken)))
                .thenThrow(new InvalidTokenException("Invalid token"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie("__Host-refresh", invalidToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Authentication Error"))
                .andExpect(jsonPath("$.message").value("Invalid token"));
    }

    @Test
    void logout_ShouldExpireCookies() throws Exception {
        String refreshToken = "valid-refresh-token";
        doNothing().when(authServiceImpl).logout(refreshToken);

        mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(new Cookie("__Host-refresh", refreshToken)))
                .andExpect(status().isOk());

        verify(authServiceImpl).logout(refreshToken);
        verify(cookieServiceImpl).expireAllCookies(any());
    }

    @Test
    void logout_WhenNoCookie_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authServiceImpl);
        verifyNoInteractions(cookieServiceImpl);
    }

    @Test
    void login_WhenInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        UserLoginRequest request = new UserLoginRequest("wrong@example.com", "wrongpass");

        when(authServiceImpl.login(any(UserLoginRequest.class)))
                .thenThrow(new InvalidPasswordException());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Incorrect Password"))
                .andExpect(jsonPath("$.message").value("Invalid password"));
    }

    @Test
    void register_WhenUserExists_ShouldReturnConflict() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest(
                "John", "Doe", "Smith", LocalDate.of(1990, 1, 1),
                "exists@example.com", "password123", Role.USER
        );

        when(authServiceImpl.register(any(UserRegisterRequest.class)))
                .thenThrow(new UserExistsException("User already exists"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("User Exists Error"))
                .andExpect(jsonPath("$.message").value("User already exists"));
    }
}