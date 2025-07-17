package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.UserDTO;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransferService;
import com.example.bankcards.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;
    private final CardService cardService;
    private final TransferService transferService;

    @GetMapping("/cards")
    public ResponseEntity<Page<CardDTO>> getUserCards(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String search,
            @PageableDefault(sort = "createdAt", direction = DESC) Pageable pageable) {

        Long userId = ((User) userDetails).getUserId();
        log.info("User {} requested cards list", userId);
        return ResponseEntity.ok(cardService.getUserCards(userId, search, pageable));
    }

    @GetMapping("/cards/{cardNumber}/balance")
    public ResponseEntity<BigDecimal> getCardBalance(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String cardNumber) {

        Long userId = ((User) userDetails).getUserId();
        log.info("User {} requested balance for card {}", userId, cardNumber);
        return ResponseEntity.ok(cardService.getCardBalance(userId, cardNumber));
    }

    @PostMapping("/cards/{cardNumber}/block")
    public ResponseEntity<Void> blockCard(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String cardNumber) {

        Long userId = ((User) userDetails).getUserId();
        log.info("User {} requested to block card {}", userId, cardNumber);
        cardService.blockCard(userId, cardNumber);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transferBetweenCards(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TransferRequest request) {

        Long userId = ((User) userDetails).getUserId();
        log.info("User {} initiated transfer: {}", userId, request);

        transferService.transferBetweenUserCards(
                userId,
                request.getFromCardNumber(),
                request.getToCardNumber(),
                request.getAmount()
        );

        return ResponseEntity.ok().build();
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getUserProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = ((User) userDetails).getUserId();
        log.info("User {} requested profile", userId);
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }
}
