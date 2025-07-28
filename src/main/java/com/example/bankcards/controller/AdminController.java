package com.example.bankcards.controller;

import com.example.bankcards.dto.cards.CardDTO;
import com.example.bankcards.dto.cards.CardReplenishmentRequest;
import com.example.bankcards.dto.cards.UserCardOperationRequest;
import com.example.bankcards.dto.cards.UserCardRequest;
import com.example.bankcards.dto.users.UpdateUserRequest;
import com.example.bankcards.dto.users.UserDTO;
import com.example.bankcards.dto.users.UserRegisterRequest;
import com.example.bankcards.service.admin.AdminServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
@Tag(name = "Admin API", description = "Операции для администраторов")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {
    private final AdminServiceImpl adminServiceImpl;

    @Operation(summary = "Создать карту", description = "Создает новую карту для указанного пользователя")
    @ApiResponse(responseCode = "200", description = "Карта успешно создана",
            content = @Content(schema = @Schema(implementation = CardDTO.class)))
    @PostMapping("/cards/create")
    public ResponseEntity<CardDTO> createCard(@RequestBody @Valid UserCardRequest request) {
        log.info("Admin creating card for user {}", request.getUserId());
        return ResponseEntity.ok(adminServiceImpl.createCard(request.getUserId()));
    }

    @Operation(summary = "Активировать карту", description = "Активирует указанную карту пользователя")
    @ApiResponse(responseCode = "200", description = "Карта активирована",
            content = @Content(schema = @Schema(implementation = CardDTO.class)))
    @PostMapping("/cards/activate")
    public ResponseEntity<CardDTO> activateCard(
            @RequestBody @Valid UserCardOperationRequest request) {
        return ResponseEntity.ok(
                adminServiceImpl.setActiveStatus(request.getUserId(), request.getCardNumber())
        );
    }

    @Operation(summary = "Заблокировать карту", description = "Блокирует указанную карту пользователя")
    @ApiResponse(responseCode = "200", description = "Карта заблокирована",
            content = @Content(schema = @Schema(implementation = CardDTO.class)))
    @PostMapping("/cards/block")
    public ResponseEntity<CardDTO> blockCard(
            @RequestBody @Valid UserCardOperationRequest request) {
        return ResponseEntity.ok(
                adminServiceImpl.blockCard(request.getUserId(), request.getCardNumber())
        );
    }

    @Operation(summary = "Удалить карту", description = "Удаляет указанную карту пользователя")
    @ApiResponse(responseCode = "204", description = "Карта удалена")
    @DeleteMapping("/cards/delete/{cardNumber}/user/{userId}")
    public ResponseEntity<Void> deleteCard(
            @Parameter(description = "Последние 4 цифры номера карты", example = "7890", required = true)
            @PathVariable String cardNumber,
            @Parameter(description = "ID пользователя", example = "1", required = true)
            @PathVariable Long userId) {
        adminServiceImpl.deleteCard(userId, cardNumber);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Установить баланс", description = "Изменяет баланс указанной карты")
    @ApiResponse(responseCode = "200", description = "Баланс обновлен",
            content = @Content(schema = @Schema(implementation = CardDTO.class)))
    @PostMapping("/cards/set_balance")
    public ResponseEntity<CardDTO> setBalance(@Valid @RequestBody CardReplenishmentRequest request) {
        log.info("Admin update balance for user: {} card number: {}", request.getUserId(), request.getCardNumber());
        return ResponseEntity.ok(adminServiceImpl.updateUserBalance(request.getUserId(),
                request.getCardNumber(),
                request.getBalance()));
    }

    @Operation(summary = "Получить все карты", description = "Возвращает список всех карт в системе")
    @ApiResponse(responseCode = "200", description = "Список карт",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CardDTO.class))))
    @GetMapping("/cards/get_all_info")
    public ResponseEntity<List<CardDTO>> getAllCards() {
        return ResponseEntity.ok(adminServiceImpl.getAllCards());
    }


    @Operation(summary = "Получить карты пользователя",
            description = "Возвращает все карты указанного пользователя")
    @ApiResponse(responseCode = "200", description = "Список карт пользователя",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CardDTO.class))))
    @GetMapping("/cards/get_by_user_id/{userId}")
    public ResponseEntity<List<CardDTO>> getUserCards(
            @Parameter(description = "ID пользователя", example = "1", required = true)
            @PathVariable Long userId) {
        return ResponseEntity.ok(adminServiceImpl.getUserCards(userId));
    }

    @Operation(summary = "Получить всех пользователей",
            description = "Возвращает список всех пользователей системы")
    @ApiResponse(responseCode = "200", description = "Список пользователей",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserDTO.class))))
    @GetMapping("/users/get_all_info")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(adminServiceImpl.getAllUsers());
    }

    @Operation(summary = "Получить пользователя по ID",
            description = "Возвращает данные пользователя по его идентификатору")
    @ApiResponse(responseCode = "200", description = "Данные пользователя",
            content = @Content(schema = @Schema(implementation = UserDTO.class)))
    @GetMapping("/users/get_by_user_id/{userId}")
    public ResponseEntity<UserDTO> getUserById(
            @Parameter(description = "ID пользователя", example = "1", required = true)
            @PathVariable Long userId) {
        return ResponseEntity.ok(adminServiceImpl.getUserById(userId));
    }

    @Operation(summary = "Получить пользователя по email",
            description = "Возвращает данные пользователя по его email")
    @ApiResponse(responseCode = "200", description = "Данные пользователя",
            content = @Content(schema = @Schema(implementation = UserDTO.class)))
    @GetMapping("/users/get_by_email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(
            @Parameter(description = "Email пользователя", example = "user@example.com", required = true)
            @PathVariable String email) {
        return ResponseEntity.ok(adminServiceImpl.getUserByEmail(email));
    }

    @Operation(summary = "Получить пользователя по телефону",
            description = "Возвращает данные пользователя по номеру телефона")
    @ApiResponse(responseCode = "200", description = "Данные пользователя",
            content = @Content(schema = @Schema(implementation = UserDTO.class)))
    @GetMapping("/users/get_by_phone_number/{phoneNumber}")
    public ResponseEntity<UserDTO> getUserByPhoneNumber(
            @Parameter(description = "Номер телефона", example = "+79123456789", required = true)
            @PathVariable String phoneNumber) {
        return ResponseEntity.ok(adminServiceImpl.getUserByPhone(phoneNumber));
    }

    @Operation(summary = "Создать пользователя",
            description = "Регистрирует нового пользователя в системе")
    @ApiResponse(responseCode = "200", description = "Пользователь создан",
            content = @Content(schema = @Schema(implementation = UserDTO.class)))
    @PostMapping("/users/create")
    public ResponseEntity<UserDTO> createUser(@RequestBody @Valid UserRegisterRequest request) {
        return ResponseEntity.ok(adminServiceImpl.createUser(request));
    }

    @Operation(summary = "Обновить пользователя",
            description = "Обновляет данные указанного пользователя")
    @ApiResponse(responseCode = "200", description = "Данные обновлены",
            content = @Content(schema = @Schema(implementation = UserDTO.class)))
    @PatchMapping("/users/update/{userId}")
    public ResponseEntity<UserDTO> updateUser(
            @Parameter(description = "ID пользователя", example = "1", required = true)
            @PathVariable Long userId,
            @RequestBody @Valid UpdateUserRequest request) {
        return ResponseEntity.ok(adminServiceImpl.updateUser(userId, request));
    }

    @Operation(summary = "Удалить пользователя", description = "Удаляет пользователя из системы")
    @ApiResponse(responseCode = "204", description = "Пользователь удален")
    @DeleteMapping("/users/delete/{userId}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID пользователя", example = "1", required = true)
            @PathVariable Long userId) {
        adminServiceImpl.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
