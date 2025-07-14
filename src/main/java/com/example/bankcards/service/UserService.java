package com.example.bankcards.service;

import com.example.bankcards.dto.UserDTO;
import com.example.bankcards.dto.UserRegisterRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.EmailBusyException;
import com.example.bankcards.exception.PhoneNumberBusyException;
import com.example.bankcards.exception.UserExistsException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public User getUserByEmailOrPhoneNumber(String email, String phoneNumber) {
        return userRepository.findByEmailOrPhoneNumber(email, phoneNumber)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public UserDTO updateUser(String email, String phoneNumber, UserDTO userDTO) {
        var existedUser = userRepository.findByEmailOrPhoneNumber(email, phoneNumber)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String currentRefreshToken = existedUser.getRefreshToken();
        LocalDateTime currentRefreshTokenExpiry = existedUser.getRefreshTokenExpiry();

        if (userDTO.getName() != null) {
            existedUser.setName(userDTO.getName());
        }
        if (userDTO.getSurname() != null) {
            existedUser.setSurname(userDTO.getSurname());
        }
        if (userDTO.getPatronymic() != null) {
            existedUser.setPatronymic(userDTO.getPatronymic());
        }
        if (userDTO.getBirthday() != null) {
            existedUser.setBirthday(userDTO.getBirthday());
        }
        if (userDTO.getEmail() != null && !userDTO.getEmail().equals(existedUser.getEmail())) {
            if (userRepository.existsByEmail(userDTO.getEmail())) {
                throw new EmailBusyException("Email has already been used!");
            }
            existedUser.setEmail(userDTO.getEmail());
        }
        if (userDTO.getPhoneNumber() != null && !userDTO.getPhoneNumber().equals(existedUser.getPhoneNumber())) {
            if (userRepository.existsByEmail(userDTO.getPhoneNumber())) {
                throw new PhoneNumberBusyException("Email has already been used!");
            }
            existedUser.setPhoneNumber(userDTO.getPhoneNumber());
        }
        existedUser.setRefreshToken(currentRefreshToken);
        existedUser.setRefreshTokenExpiry(currentRefreshTokenExpiry);

        userRepository.save(existedUser);
        return userMapper.toDTO(existedUser);
    }

    @Transactional
    public void deleteUser(String email, String phoneNumber) {
        var user = userRepository.findByEmailOrPhoneNumber(email, phoneNumber)
                .orElseThrow(() -> new UserNotFoundException("User not found. Maybe it was deleted before."));
        userRepository.delete(user);
    }
}
