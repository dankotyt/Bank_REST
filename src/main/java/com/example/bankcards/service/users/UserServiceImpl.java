package com.example.bankcards.service.users;

import com.example.bankcards.dto.users.UserDTO;
import com.example.bankcards.exception.users.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.Mapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final Mapper mapper;

    @Override
    public UserDTO getUserProfile(Long userId) {
        return userRepository.findById(userId)
                .map(mapper::toDTO)
                .orElseThrow(UserNotFoundException::new);
    }
}