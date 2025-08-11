package com.example.bankcards.controller;

import com.example.bankcards.dto.cards.*;
import com.example.bankcards.dto.users.UpdateUserRequest;
import com.example.bankcards.dto.users.UserDTO;
import com.example.bankcards.dto.users.UserRegisterRequest;
import com.example.bankcards.service.admin.AdminService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@Slf4j
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AdminController {
    private final AdminService adminService;

    @PostMapping("/cards/create")
    public ResponseEntity<CardDTO> createCard(@RequestBody @Valid UserCardRequest request) {
        log.info("Admin creating card for user {}", request.getUserId());
        return ResponseEntity.ok(adminService.createCard(request.getUserId()));
    }

    @PostMapping("/cards/activate")
    public ResponseEntity<CardDTO> activateCard(
            @RequestBody @Valid UserCardOperationRequest request) {
        return ResponseEntity.ok(
                adminService.activateCard(request.getUserId(), request.getCardNumber())
        );
    }

    @PostMapping("/cards/block")
    public ResponseEntity<CardDTO> blockCard(
            @RequestBody @Valid UserCardOperationRequest request) {
        return ResponseEntity.ok(
                adminService.blockCard(request.getUserId(), request.getCardNumber())
        );
    }

   @DeleteMapping("/cards/delete/{cardNumber}/user/{userId}")
    public ResponseEntity<Void> deleteCard(
            @PathVariable String cardNumber,
            @PathVariable Long userId) {
        adminService.deleteCard(userId, cardNumber);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cards/set_balance")
    public ResponseEntity<CardDTO> setBalance(@Valid @RequestBody CardReplenishmentRequest request) {
        log.info("Admin update balance for user: {} card number: {}", request.getUserId(), request.getCardNumber());
        return ResponseEntity.ok(adminService.updateUserBalance(request.getUserId(),
                request.getCardNumber(),
                request.getBalance()));
    }

    @GetMapping("/cards/get_all_info")
    public ResponseEntity<List<CardDTO>> getAllCards() {
        return ResponseEntity.ok(adminService.getAllCards());
    }

    @GetMapping("/cards/get_by_user_id/{userId}")
    public ResponseEntity<List<CardDTO>> getUserCards(
            @PathVariable Long userId) {
        return ResponseEntity.ok(adminService.getUserCards(userId));
    }

   @GetMapping("/users/get_all_info")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/get_by_user_id/{userId}")
    public ResponseEntity<UserDTO> getUserById(
            @PathVariable Long userId) {
        return ResponseEntity.ok(adminService.getUserById(userId));
    }

    @GetMapping("/users/get_by_email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(
            @PathVariable String email) {
        return ResponseEntity.ok(adminService.getUserByEmail(email));
    }

    @PostMapping("/users/create")
    public ResponseEntity<UserDTO> createUser(@RequestBody @Valid UserRegisterRequest request) {
        return ResponseEntity.ok(adminService.createUser(request));
    }

    @PatchMapping("/users/update/{userId}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long userId,
            @RequestBody @Valid UpdateUserRequest request) {
        return ResponseEntity.ok(adminService.updateUser(userId, request));
    }

    @DeleteMapping("/users/delete/{userId}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
