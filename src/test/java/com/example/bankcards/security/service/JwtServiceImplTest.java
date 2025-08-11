package com.example.bankcards.security.service;

import com.example.bankcards.config.JwtConfig;
import com.example.bankcards.dto.users.UserDTO;
import com.example.bankcards.dto.users.UserLoginResponse;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.factory.JwtTokenFactory;
import com.example.bankcards.util.Mapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtConfig jwtConfig;

    @Mock
    private Mapper mapper;

    @Mock
    private JwtTokenFactory jwtTokenFactory;

    @InjectMocks
    private JwtServiceImpl jwtServiceImpl;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setRole(Role.USER);
    }

    @Test
    void generateTokenPair_ShouldReturnValidResponse() {
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";
        UserDTO userDTO = new UserDTO();

        when(jwtConfig.getRefreshTtl()).thenReturn(3600000L);
        when(jwtTokenFactory.createAccessToken(testUser, List.of("ROLE_USER")))
                .thenReturn(accessToken);
        when(jwtTokenFactory.createRefreshToken(testUser))
                .thenReturn(refreshToken);
        when(mapper.toDTO(testUser)).thenReturn(userDTO);

        UserLoginResponse response = jwtServiceImpl.generateTokenPair(testUser);

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
        UserDTO adminDTO = new UserDTO();

        when(jwtTokenFactory.createAccessToken(eq(adminUser), eq(List.of("ROLE_ADMIN"))))
                .thenReturn("admin.token");
        when(jwtTokenFactory.createRefreshToken(adminUser))
                .thenReturn("refresh.token");
        when(mapper.toDTO(adminUser)).thenReturn(adminDTO);

        UserLoginResponse response = jwtServiceImpl.generateTokenPair(adminUser);

        assertNotNull(response);
        assertEquals("admin.token", response.getAccessToken());
        assertEquals("refresh.token", response.getRefreshToken());
        assertEquals(adminDTO, response.getUserDTO());
        verify(jwtTokenFactory).createAccessToken(eq(adminUser), eq(List.of("ROLE_ADMIN")));
        verify(userRepository).save(adminUser);
    }

    @Test
    void extractAccessTokenFromRequest_WithBearerToken_ShouldReturnToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer test.token");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String token = jwtServiceImpl.extractAccessTokenFromRequest();
        assertEquals("test.token", token);

        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void extractAccessTokenFromRequest_WithoutBearerToken_ShouldReturnNull() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic credentials");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String token = jwtServiceImpl.extractAccessTokenFromRequest();
        assertNull(token);

        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void extractAccessTokenFromRequest_NoRequestContext_ShouldReturnNull() {
        RequestContextHolder.resetRequestAttributes();

        String token = jwtServiceImpl.extractAccessTokenFromRequest();
        assertNull(token);
    }
}