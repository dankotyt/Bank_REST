package com.example.bankcards.security.cookie;

import com.example.bankcards.config.JwtConfig;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CookieServiceImplTest {

    @Mock
    private JwtConfig jwtConfig;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private CookieServiceImpl cookieServiceImpl;

    private final String testToken = "test.token.value";
    private final long testTtl = 3600000; // 1 hour in ms

    @Test
    void setAccessTokenCookie_ShouldSetCorrectCookie() {
        when(jwtConfig.getAccessTtl()).thenReturn(testTtl);

        cookieServiceImpl.setAccessTokenCookie(response, testToken);

        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), headerCaptor.capture());

        String cookieHeader = headerCaptor.getValue();
        assertThat(cookieHeader)
                .contains("__Host-auth-token=" + testToken)
                .contains("HttpOnly")
                .contains("Secure")
                .contains("Path=/")
                .contains("SameSite=Strict")
                .contains("Max-Age=" + (testTtl / 1000));
    }

    @Test
    void setRefreshTokenCookie_ShouldSetCorrectCookie() {
        when(jwtConfig.getRefreshTtl()).thenReturn(testTtl);

        cookieServiceImpl.setRefreshTokenCookie(response, testToken);

        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), headerCaptor.capture());

        String cookieHeader = headerCaptor.getValue();
        assertThat(cookieHeader)
                .contains("__Host-refresh=" + testToken)
                .contains("HttpOnly")
                .contains("Secure")
                .contains("Path=/")
                .contains("SameSite=Strict")
                .contains("Max-Age=" + (testTtl / 1000));
    }

    @Test
    void expireAllCookies_ShouldExpireBothCookies() {
        cookieServiceImpl.expireAllCookies(response);

        verify(response, times(2)).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());

        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
        verify(response, times(2)).addHeader(eq(HttpHeaders.SET_COOKIE), headerCaptor.capture());

        List<String> cookieHeaders = headerCaptor.getAllValues();
        assertThat(cookieHeaders.get(0)).contains("__Host-auth-token=");
        assertThat(cookieHeaders.get(1)).contains("__Host-refresh=");
    }
}