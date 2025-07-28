package com.example.bankcards.controller;

import com.example.bankcards.dto.cards.CardDTO;
import com.example.bankcards.dto.cards.TransferRequest;
import com.example.bankcards.dto.cards.TransferResponse;
import com.example.bankcards.dto.users.UserDTO;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.card.CardServiceImpl;
import com.example.bankcards.service.transfer.TransferServiceImpl;
import com.example.bankcards.service.user.UserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "User API", description = "Операции для авторизованных пользователей")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserServiceImpl userServiceImpl;
    private final CardServiceImpl cardServiceImpl;
    private final TransferServiceImpl transferServiceImpl;

    @Operation(summary = "Получить карты",
            description = "Возвращает список карт текущего пользователя с пагинацией")
    @ApiResponse(responseCode = "200", description = "Список карт",
            content = @Content(schema = @Schema(implementation = Page.class)))
    @GetMapping("/cards")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CardDTO>> getUserCards(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Поисковый запрос (опционально)")
            @RequestParam(required = false) String search,
            @PageableDefault(sort = "expiryDate", direction = DESC) Pageable pageable) {

        Long userId = user.getUserId();
        log.info("User {} requested cards list", userId);
        return ResponseEntity.ok(cardServiceImpl.getUserCards(userId, search, pageable));
    }

    @Operation(summary = "Получить баланс карты",
            description = "Возвращает баланс указанной карты пользователя")
    @ApiResponse(responseCode = "200", description = "Текущий баланс",
            content = @Content(schema = @Schema(implementation = BigDecimal.class)))
    @GetMapping("/cards/{cardNumber}/balance")
    public ResponseEntity<BigDecimal> getCardBalance(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Последние 4 цифры номера карты", example = "7890", required = true)
            @PathVariable String cardNumber) {

        Long userId = user.getUserId();
        log.info("User {} requested balance for card {}", userId, cardNumber);
        return ResponseEntity.ok(cardServiceImpl.getCardBalance(userId, cardNumber));
    }

    @Operation(summary = "Блокировка карты", description = "Блокирует указанную карту пользователя")
    @ApiResponse(responseCode = "200", description = "Карта заблокирована")
    @PostMapping("/cards/{cardNumber}/block")
    public ResponseEntity<Void> blockCard(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Последние 4 цифры номера карты", example = "7890", required = true)
            @PathVariable String cardNumber) {

        Long userId = user.getUserId();
        log.info("User {} requested to block card {}", userId, cardNumber);
        cardServiceImpl.blockCard(userId, cardNumber);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Перевод средств",
            description = "Выполняет перевод между картами текущего пользователя")
    @ApiResponse(responseCode = "200",
            description = "Перевод выполнен",
            content = @Content(schema = @Schema(implementation = TransferResponse.class)))
    @ApiResponse(responseCode = "400",
            description = "Недостаточно средств или другие ошибки")
    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transferBetweenCards(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody TransferRequest request) {

        Long userId = user.getUserId();
        log.info("User {} initiated transfer: {}", userId, request);

        TransferResponse response = transferServiceImpl.transferBetweenUserCards(
                userId,
                request.getFromCardNumber(),
                request.getToCardNumber(),
                request.getAmount()
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получить профиль",
            description = "Возвращает данные текущего пользователя")
    @ApiResponse(responseCode = "200",
            description = "Данные пользователя",
            content = @Content(schema = @Schema(implementation = UserDTO.class)))
    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getUserProfile(
            @AuthenticationPrincipal User user) {

        Long userId = user.getUserId();
        log.info("User {} requested profile", userId);
        return ResponseEntity.ok(userServiceImpl.getUserProfile(userId));
    }
}
