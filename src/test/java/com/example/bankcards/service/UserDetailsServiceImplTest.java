package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private final String testEmail = "test@example.com";
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(1L)
                .email(testEmail)
                .password("password")
                .build();
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername(testEmail);

        assertThat(userDetails.getUsername()).isEqualTo(testEmail);
        assertThat(userDetails.getPassword()).isEqualTo(testUser.getPassword());
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(testEmail))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found with email: " + testEmail);
    }
}