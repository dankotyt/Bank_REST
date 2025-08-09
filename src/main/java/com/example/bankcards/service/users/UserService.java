package com.example.bankcards.service.users;

import com.example.bankcards.dto.users.UserDTO;

public interface UserService {
    UserDTO getUserProfile(Long userId);
}
