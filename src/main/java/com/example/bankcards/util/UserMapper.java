package com.example.bankcards.util;

import com.example.bankcards.dto.UserDTO;
import com.example.bankcards.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getUserId());
        dto.setName(user.getName());
        dto.setSurname(user.getSurname());
        dto.setPatronymic(user.getPatronymic());
        dto.setBirthday(user.getBirthday());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        return dto;
    }
}
