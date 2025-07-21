package com.example.bankcards.security;

import com.example.bankcards.config.JwtConfig;
import com.example.bankcards.dto.users.UserDTO;
import com.example.bankcards.dto.users.UserLoginResponse;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.Mapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtConfig jwtConfig;

    @Mock
    private Mapper mapper;

    @Mock
    private JwtTokenFactory jwtTokenFactory;

    @InjectMocks
    private JwtService jwtService;

    private User testUser;
    private final Set<String> revokedTokens = ConcurrentHashMap.newKeySet();

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setRole(Role.USER);

        // Используем рефлексию для установки поля revokedTokens, так как оно final
        try {
            var field = JwtService.class.getDeclaredField("revokedTokens");
            field.setAccessible(true);
            field.set(jwtService, revokedTokens);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void generateTokenPair_ShouldReturnValidResponse() {
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";
        UserDTO userDTO = new UserDTO();

        when(jwtTokenFactory.createAccessToken(testUser, List.of("ROLE_USER"))).thenReturn(accessToken);
        when(jwtTokenFactory.createRefreshToken(testUser)).thenReturn(refreshToken);
        when(mapper.toDTO(testUser)).thenReturn(userDTO);
        when(jwtConfig.getRefreshTtl()).thenReturn(3600000L); // 1 hour

        UserLoginResponse response = jwtService.generateTokenPair(testUser);

        assertNotNull(response);
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
        assertEquals(userDTO, response.getUserDTO());

        verify(userRepository).save(testUser);
        assertNotNull(testUser.getRefreshToken());
        assertNotNull(testUser.getRefreshTokenExpiry());
    }

    @Test
    void generateTokenPair_ForAdminUser_ShouldIncludeAdminRole() {
        User adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(Role.ADMIN);

        when(jwtTokenFactory.createAccessToken(eq(adminUser), eq(List.of("ROLE_ADMIN"))))
                .thenReturn("admin.token");
        when(jwtTokenFactory.createRefreshToken(adminUser)).thenReturn("refresh.token");
        when(mapper.toDTO(adminUser)).thenReturn(new UserDTO());
        when(jwtConfig.getRefreshTtl()).thenReturn(3600000L);

        UserLoginResponse response = jwtService.generateTokenPair(adminUser);

        assertNotNull(response);
        assertEquals("admin.token", response.getAccessToken());
        assertEquals("refresh.token", response.getRefreshToken());
        verify(jwtTokenFactory).createAccessToken(eq(adminUser), eq(List.of("ROLE_ADMIN")));
        verify(userRepository).save(adminUser);
    }

    @Test
    void generateTokenPair_ForRegularUser_ShouldIncludeUserRole() {
        User regularUser = new User();
        regularUser.setEmail("user@example.com");
        regularUser.setRole(Role.USER);

        when(jwtTokenFactory.createAccessToken(eq(regularUser), eq(List.of("ROLE_USER"))))
                .thenReturn("user.token");
        when(jwtTokenFactory.createRefreshToken(regularUser)).thenReturn("refresh.token");
        when(mapper.toDTO(regularUser)).thenReturn(new UserDTO());
        when(jwtConfig.getRefreshTtl()).thenReturn(3600000L);

        UserLoginResponse response = jwtService.generateTokenPair(regularUser);

        assertNotNull(response);
        assertEquals("user.token", response.getAccessToken());
        assertEquals("refresh.token", response.getRefreshToken());
        verify(jwtTokenFactory).createAccessToken(eq(regularUser), eq(List.of("ROLE_USER")));
        verify(userRepository).save(regularUser);
    }

//    @Test
//    void parseToken_ValidToken_ShouldReturnClaims() {
//        String token = "valid.token.here";
//        Claims expectedClaims = Jwts.claims().subject("test@example.com").build();
//
//        JwtParserBuilder parserBuilder = mock(JwtParserBuilder.class);
//        JwtParser parser = mock(JwtParser.class);
//        Jws<Claims> jws = mock(Jws.class);
//
//        try (MockedStatic<Jwts> mockedJwts = mockStatic(Jwts.class)) {
//            mockedJwts.when(Jwts::parser).thenReturn(parserBuilder);
//            when(parserBuilder.verifyWith(secretKey)).thenReturn(parserBuilder);
//            when(parserBuilder.build()).thenReturn(parser);
//            when(parser.parseSignedClaims(token)).thenReturn(jws);
//            when(jws.getPayload()).thenReturn(expectedClaims);
//
//            Claims claims = jwtService.parseToken(token);
//
//            assertNotNull(claims);
//            assertEquals(expectedClaims, claims);
//        }
//    }
//
//    @Test
//    void parseToken_InvalidToken_ShouldThrowException() {
//        String invalidToken = "invalid.token";
//
//        JwtParserBuilder parserBuilder = mock(JwtParserBuilder.class);
//        JwtParser parser = mock(JwtParser.class);
//
//        try (MockedStatic<Jwts> mockedJwts = mockStatic(Jwts.class)) {
//            mockedJwts.when(Jwts::parser).thenReturn(parserBuilder);
//            when(parserBuilder.verifyWith(secretKey)).thenReturn(parserBuilder);
//            when(parserBuilder.build()).thenReturn(parser);
//            when(parser.parseSignedClaims(invalidToken))
//                    .thenThrow(new RuntimeException("Invalid token"));
//
//            assertThrows(InvalidTokenException.class, () -> jwtService.parseToken(invalidToken));
//        }
//    }

    @Test
    void extractUsername_ShouldReturnSubjectFromToken() {
        String token = "token";
        Claims claims = Jwts.claims().subject("test@example.com").build();

        JwtService spyService = spy(jwtService);
        doReturn(claims).when(spyService).parseToken(token);

        String username = spyService.extractUsername(token);

        assertEquals("test@example.com", username);
    }

    @Test
    void isTokenExpired_WhenExpired_ShouldReturnTrue() {
        String token = "expired.token";
        Date pastDate = new Date(System.currentTimeMillis() - 10000);

        JwtService spyService = spy(jwtService);
        doReturn(pastDate).when(spyService).extractExpiration(token);

        boolean isExpired = spyService.isTokenExpired(token);

        assertTrue(isExpired);
    }

    @Test
    void revokeToken_WhenNotExpired_ShouldAddToRevokedTokens() {
        String token = "valid.token";

        JwtService spyService = spy(jwtService);
        doReturn(false).when(spyService).isTokenExpired(token);

        spyService.revokeToken(token);

        assertTrue(revokedTokens.contains(token));
    }

    @Test
    void revokeToken_WhenExpired_ShouldNotAddToRevokedTokens() {
        String token = "expired.token";

        JwtService spyService = spy(jwtService);
        doReturn(true).when(spyService).isTokenExpired(token);

        spyService.revokeToken(token);

        assertFalse(revokedTokens.contains(token));
    }

    @Test
    void isTokenValid_WhenAllConditionsMet_ShouldReturnTrue() {
        String token = "valid.token";

        JwtService spyService = spy(jwtService);
        doReturn("test@example.com").when(spyService).extractUsername(token);
        doReturn(false).when(spyService).isTokenExpired(token);

        boolean isValid = spyService.isTokenValid(token, testUser);

        assertTrue(isValid);
    }

    @Test
    void isTokenValid_WhenUsernameMismatch_ShouldReturnFalse() {
        String token = "valid.token";

        JwtService spyService = spy(jwtService);
        doReturn("other@example.com").when(spyService).extractUsername(token);

        boolean isValid = spyService.isTokenValid(token, testUser);

        assertFalse(isValid);
    }

    @Test
    void isTokenValid_WhenTokenRevoked_ShouldReturnFalse() {
        String token = "revoked.token";
        revokedTokens.add(token);

        JwtService spyService = spy(jwtService);
        doReturn("test@example.com").when(spyService).extractUsername(token);
        boolean isValid = spyService.isTokenValid(token, testUser);

        assertFalse(isValid);
    }

    @Test
    void extractAccessTokenFromRequest_WithBearerToken_ShouldReturnToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer test.token");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String token = jwtService.extractAccessTokenFromRequest();

        assertEquals("test.token", token);
    }

    @Test
    void extractAccessTokenFromRequest_WithoutBearerToken_ShouldReturnNull() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic credentials");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String token = jwtService.extractAccessTokenFromRequest();

        assertNull(token);
    }

    @Test
    void extractAccessTokenFromRequest_NoRequestContext_ShouldReturnNull() {
        RequestContextHolder.resetRequestAttributes();

        String token = jwtService.extractAccessTokenFromRequest();

        assertNull(token);
    }

    @Test
    void extractExpiration_ShouldReturnExpirationDate() {
        String token = "test.token";
        Date expectedDate = new Date(System.currentTimeMillis() + 10000);

        JwtService spyService = spy(jwtService);
        doReturn(expectedDate).when(spyService).extractClaim(eq(token), any(Function.class));

        Date result = spyService.extractExpiration(token);

        assertEquals(expectedDate, result);
    }

    @Test
    void isTokenRevoked_WhenTokenRevoked_ShouldReturnTrue() {
        String token = "revoked.token";

        // Используем рефлексию для доступа к revokedTokens
        try {
            Field field = JwtService.class.getDeclaredField("revokedTokens");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Set<String> revokedTokens = (Set<String>) field.get(jwtService);
            revokedTokens.add(token);
        } catch (Exception e) {
            fail("Failed to set revoked token", e);
        }

        boolean result = jwtService.isTokenRevoked(token);

        assertTrue(result);
    }

    @Test
    void isTokenRevoked_WhenTokenNotRevoked_ShouldReturnFalse() {
        String token = "valid.token";

        boolean result = jwtService.isTokenRevoked(token);

        assertFalse(result);
    }

    @Test
    void isTokenValid_WhenTokenParsingFails_ShouldReturnFalseAndLogError() {
        String token = "invalid.token";
        User user = new User();
        user.setEmail("test@example.com");

        JwtService spyService = spy(jwtService);
        doThrow(new RuntimeException("Parsing failed")).when(spyService).extractUsername(token);

        boolean result = spyService.isTokenValid(token, user);

        assertFalse(result);
    }
}