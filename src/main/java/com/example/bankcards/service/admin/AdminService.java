package com.example.bankcards.service.admin;

import com.example.bankcards.dto.cards.CardDTO;
import com.example.bankcards.dto.users.UpdateUserRequest;
import com.example.bankcards.dto.users.UserDTO;
import com.example.bankcards.dto.users.UserRegisterRequest;

import java.math.BigDecimal;
import java.util.List;

public interface AdminService {
    CardDTO createCard(Long id);
    CardDTO activateCard(Long userId, String cardNumber);
    CardDTO blockCard(Long userId, String cardNumber);
    void deleteCard(Long userId, String cardNumber);
    CardDTO updateUserBalance(Long userId, String cardNumber, BigDecimal balance);
    List<CardDTO> getAllCards();
    List<CardDTO> getUserCards(Long userId);
    List<UserDTO> getAllUsers();
    UserDTO getUserById(Long userId);
    UserDTO getUserByEmail(String email);
    UserDTO createUser(UserRegisterRequest request);
    UserDTO updateUser(Long userId, UpdateUserRequest request);
    void deleteUser(Long userId);
}
