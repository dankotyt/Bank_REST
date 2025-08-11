package com.example.bankcards.service.users;

import com.example.bankcards.dto.users.UserDTO;
import com.example.bankcards.exception.users.UserNotFoundException;

/**
 * Сервис для работы с профилями пользователей.
 */
public interface UserService {
    /**
     * Получает профиль пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return DTO с информацией о пользователе
     * @throws UserNotFoundException если пользователь с указанным ID не найден
     */
    UserDTO getUserProfile(Long userId);
}
