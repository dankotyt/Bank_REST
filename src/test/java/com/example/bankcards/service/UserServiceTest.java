package com.example.bankcards.service;

import com.example.bankcards.dto.users.UserDTO;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.users.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.users.UserServiceImpl;
import com.example.bankcards.util.Mapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Mapper mapper;

    @InjectMocks
    private UserServiceImpl userService;

    private final Long testUserId = 1L;
    private User testUser;
    private UserDTO testUserDto;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(testUserId)
                .email("test@example.com")
                .build();

        testUserDto = new UserDTO();
        testUserDto.setEmail(testUser.getEmail());
    }

    @Test
    void getUserProfile_ShouldReturnUserDto_WhenUserExists() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(mapper.toDTO(testUser)).thenReturn(testUserDto);

        UserDTO result = userService.getUserProfile(testUserId);

        assertThat(result).isEqualTo(testUserDto);
        verify(userRepository).findById(testUserId);
        verify(mapper).toDTO(testUser);
    }

    @Test
    void getUserProfile_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserProfile(testUserId))
                .isInstanceOf(UserNotFoundException.class);
    }
}