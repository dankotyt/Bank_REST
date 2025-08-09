package com.example.bankcards.controller;

import com.example.bankcards.dto.cards.CardDTO;
import com.example.bankcards.dto.cards.TransferRequest;
import com.example.bankcards.dto.cards.TransferResponse;
import com.example.bankcards.dto.users.UserDTO;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.cards.CardService;
import com.example.bankcards.service.transfers.TransferService;
import com.example.bankcards.service.users.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ROLE_USER')")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;
    private final CardService cardService;
    private final TransferService transferService;

    @GetMapping("/cards")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CardDTO>> getUserCards(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String search,
            @PageableDefault(sort = "expiryDate", direction = DESC) Pageable pageable) {

        Long userId = user.getUserId();
        log.info("User {} requested cards list", userId);
        return ResponseEntity.ok(cardService.getUserCards(userId, search, pageable));
    }

    @GetMapping("/cards/{cardNumber}/balance")
    public ResponseEntity<BigDecimal> getCardBalance(
            @AuthenticationPrincipal User user,
            @PathVariable String cardNumber) {

        Long userId = user.getUserId();
        log.info("User {} requested balance for card {}", userId, cardNumber);
        return ResponseEntity.ok(cardService.getCardBalance(userId, cardNumber));
    }

    @PostMapping("/cards/{cardNumber}/block")
    public ResponseEntity<Void> blockCard(
            @AuthenticationPrincipal User user,
            @PathVariable String cardNumber) {

        Long userId = user.getUserId();
        log.info("User {} requested to block card {}", userId, cardNumber);
        cardService.blockCard(userId, cardNumber);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transferBetweenCards(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody TransferRequest request) {

        Long userId = user.getUserId();
        log.info("User {} initiated transfer: {}", userId, request);

        TransferResponse response = transferService.transferBetweenUserCards(
                userId,
                request.getFromCardNumber(),
                request.getToCardNumber(),
                request.getAmount()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getUserProfile(
            @AuthenticationPrincipal User user) {
        Long userId = user.getUserId();
        log.info("User {} requested profile", userId);
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }
}