package com.example.bankcards.service;

import com.example.bankcards.dto.users.UserDTO;
import com.example.bankcards.dto.users.UserLoginRequest;
import com.example.bankcards.dto.users.UserLoginResponse;
import com.example.bankcards.dto.users.UserRegisterRequest;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.auth.InvalidPasswordException;
import com.example.bankcards.exception.auth.InvalidTokenException;
import com.example.bankcards.exception.users.UserExistsException;
import com.example.bankcards.exception.users.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.parser.JwtParser;
import com.example.bankcards.security.service.JwtService;
import com.example.bankcards.security.validator.JwtTokenValidator;
import com.example.bankcards.service.auth.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtParser jwtParser;

    @Mock
    private JwtTokenValidator jwtTokenValidator;

    @InjectMocks
    private AuthServiceImpl authService;


    private User testUser;
    private UserDTO testUserDTO;
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(1L)
                .name("John")
                .surname("Doe")
                .patronymic("Smith")
                .birthday(LocalDate.of(1990, 1, 1))
                .email("john@example.com")
                .password("encoded-password")
                .role(Role.USER)
                .createdAt(now)
                .build();

        testUserDTO = new UserDTO(
                1L, "John", "Doe", "Smith",
                LocalDate.of(1990, 1, 1), "john@example.com",
                now
        );
    }

    @Test
    void register_ShouldReturnTokensWithUserDTO() {
        UserRegisterRequest request = new UserRegisterRequest(
                "John", "Doe", "Smith", LocalDate.of(1990, 1, 1),
                "john@example.com", "password123", Role.USER
        );
        UserLoginResponse expectedResponse = new UserLoginResponse(
                "access", "refresh", testUserDTO
        );

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(jwtService.generateTokenPair(any(User.class))).thenReturn(expectedResponse);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserLoginResponse result = authService.register(request);

        assertThat(result).isEqualTo(expectedResponse);
        assertThat(result.getUserDTO()).isNotNull();
        assertThat(result.getUserDTO().getEmail()).isEqualTo("john@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_WithExistingEmail_ShouldThrowException() {
        UserRegisterRequest request = new UserRegisterRequest(
                "John", "Doe", "Smith", LocalDate.of(1990, 1, 1),
                "john@example.com", "password123", Role.USER
        );
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UserExistsException.class)
                .hasMessage("User with this email or phone number already exists!");
    }

    @Test
    void login_ShouldReturnTokensWithUserDTO() {
        UserLoginRequest request = new UserLoginRequest("john@example.com", "password123");
        UserLoginResponse expectedResponse = new UserLoginResponse(
                "access", "refresh", testUserDTO
        );

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);
        when(jwtService.generateTokenPair(testUser)).thenReturn(expectedResponse);

        UserLoginResponse result = authService.login(request);

        assertThat(result).isEqualTo(expectedResponse);
        assertThat(result.getUserDTO()).isNotNull();
        assertThat(result.getUserDTO().getId()).isEqualTo(1L);
    }

    @Test
    void login_WithInvalidPassword_ShouldThrowInvalidPasswordException() {
        UserLoginRequest request = new UserLoginRequest("john@example.com", "wrong-password");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidPasswordException.class);

        verify(passwordEncoder).matches("wrong-password", "encoded-password");
        verifyNoInteractions(jwtService);
    }

    @Test
    void login_WithInvalidEmail_ShouldThrowException() {
        UserLoginRequest request = new UserLoginRequest("wrong@example.com", "password123");
        when(userRepository.findByEmail("wrong@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void logout_ShouldRevokeTokensAndClearRefreshToken() {
        String refreshToken = "valid-refresh-token";
        testUser.setRefreshToken(refreshToken);

        when(userRepository.findByRefreshToken(refreshToken)).thenReturn(Optional.of(testUser));
        when(jwtService.extractAccessTokenFromRequest()).thenReturn("access-token");

        authService.logout(refreshToken);

        verify(jwtTokenValidator).revokeToken("access-token");
        verify(userRepository).save(testUser);
        assertThat(testUser.getRefreshToken()).isNull();
    }

    @Test
    void logout_WhenUserNotFound_ShouldDoNothing() {
        String refreshToken = "non-existing-token";
        when(userRepository.findByRefreshToken(refreshToken)).thenReturn(Optional.empty());

        authService.logout(refreshToken);

        verifyNoInteractions(jwtService);
        verify(userRepository, never()).save(any());
    }

    @Test
    void refreshToken_ShouldReturnNewTokensWithUserDTO() {
        String refreshToken = "valid-refresh-token";
        UserLoginResponse expectedResponse = new UserLoginResponse(
                "new-access", "new-refresh", testUserDTO
        );

        testUser.setRefreshToken(refreshToken);
        testUser.setRefreshTokenExpiry(now.plusDays(1));

        when(jwtParser.extractUsername(refreshToken)).thenReturn("john@example.com");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(jwtTokenValidator.isTokenValid(refreshToken, testUser)).thenReturn(true);
        when(jwtService.generateTokenPair(testUser)).thenReturn(expectedResponse);

        UserLoginResponse result = authService.refreshToken(refreshToken);

        assertThat(result).isEqualTo(expectedResponse);
        assertThat(result.getUserDTO()).isNotNull();
    }

    @Test
    void refreshToken_WithEmptyToken_ShouldThrowException() {
        assertThatThrownBy(() -> authService.refreshToken(""))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Refresh token is empty!");
    }

    @Test
    void refreshToken_WithRevokedToken_ShouldThrowException() {
        String refreshToken = "revoked-token";
        when(jwtTokenValidator.isTokenRevoked(refreshToken)).thenReturn(true);

        assertThatThrownBy(() -> authService.refreshToken(refreshToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Token revoked");
    }

    @Test
    void refreshToken_WithTokenMismatch_ShouldThrowException() {
        String refreshToken = "wrong-token";
        testUser.setRefreshToken("different-token");

        when(jwtParser.extractUsername(refreshToken)).thenReturn("john@example.com");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(jwtTokenValidator.isTokenRevoked(refreshToken)).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken(refreshToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Discrepancy of refresh token!");
    }

    @Test
    void refreshToken_WithExpiredToken_ShouldThrowException() {
        String refreshToken = "expired-token";
        testUser.setRefreshToken(refreshToken);
        testUser.setRefreshTokenExpiry(LocalDateTime.now().minusDays(1));

        when(jwtParser.extractUsername(refreshToken)).thenReturn("john@example.com");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(jwtTokenValidator.isTokenRevoked(refreshToken)).thenReturn(false);
        when(jwtTokenValidator.isTokenValid(refreshToken, testUser)).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken(refreshToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("The refresh token is expired!");
    }

    @Test
    void refreshToken_WithInvalidToken_ShouldThrowException() {
        String refreshToken = "invalid-token";
        testUser.setRefreshToken(refreshToken);
        testUser.setRefreshTokenExpiry(LocalDateTime.now().plusDays(1));

        when(jwtParser.extractUsername(refreshToken)).thenReturn("john@example.com");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(jwtTokenValidator.isTokenRevoked(refreshToken)).thenReturn(false);
        when(jwtTokenValidator.isTokenValid(refreshToken, testUser)).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken(refreshToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Refresh token is invalid!");
    }

}