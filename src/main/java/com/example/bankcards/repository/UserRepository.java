package com.example.bankcards.repository;

import com.example.bankcards.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    Optional<User> findByEmailOrPhoneNumber(String email, String phoneNumber);
    Optional<User> findByPhoneNumber(String phoneNumber);
    Optional<User> findByRefreshToken(String token);
    boolean existsByEmailOrPhoneNumber(@NotNull(message = "The field couldn`t be empty!") @Email(message = "Incorrect email or phone number!")
                                       String email, String phoneNumber);
    boolean existsByPhoneNumber(@NotNull(message = "The field couldn`t be empty!") @Email(message = "Incorrect phone number!")
                                String phoneNumber);
    boolean existsByEmail(@NotNull(message = "The field couldn`t be empty!") @Email(message = "Incorrect email!")
                          String email);

}
