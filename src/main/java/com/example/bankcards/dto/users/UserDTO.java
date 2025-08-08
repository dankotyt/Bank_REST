package com.example.bankcards.dto.users;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String name;
    private String surname;
    private String patronymic;
    private LocalDate birthday;
    private String email;
    private LocalDateTime createdAt;
}
