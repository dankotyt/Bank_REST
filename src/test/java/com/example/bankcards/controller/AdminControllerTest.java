package com.example.bankcards.controller;

import com.example.bankcards.config.SecurityConfig;
import com.example.bankcards.dto.cards.CardDTO;
import com.example.bankcards.dto.cards.CardReplenishmentRequest;
import com.example.bankcards.dto.cards.UserCardOperationRequest;
import com.example.bankcards.dto.cards.UserCardRequest;
import com.example.bankcards.dto.users.UpdateUserRequest;
import com.example.bankcards.dto.users.UserDTO;
import com.example.bankcards.dto.users.UserRegisterRequest;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.service.admin.AdminServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
@WithMockUser(roles = "ADMIN")
class AdminControllerTest {

    @Mock
    private AdminServiceImpl adminServiceImpl;

    @InjectMocks
    private AdminController adminController;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private CardDTO testCard;
    private UserDTO testUser;
    private UserCardRequest userCardRequest;
    private UserCardOperationRequest userCardOperationRequest;
    private CardReplenishmentRequest replenishmentRequest;
    private UserRegisterRequest registerRequest;
    private UpdateUserRequest updateRequest;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        testCard = new CardDTO(
                "**** **** **** 1234", LocalDate.now().plusYears(3),
                "John Doe", BigDecimal.valueOf(1000), CardStatus.ACTIVE
        );

        testUser = new UserDTO(
                1L, "John", "Doe", "Smith",
                LocalDate.of(1990, 1, 1), "john@example.com",
                LocalDateTime.now()
        );

        userCardRequest = new UserCardRequest(1L);
        userCardOperationRequest = new UserCardOperationRequest(1L, "1234");
        replenishmentRequest = new CardReplenishmentRequest(
                1L, "**** **** **** 1234", BigDecimal.valueOf(1000)
        );
        registerRequest = new UserRegisterRequest(
                "John", "Doe", "Smith",
                LocalDate.of(1990, 1, 1),
                "john@example.com",
                "password123", Role.USER
        );
        updateRequest = new UpdateUserRequest(
                "Johny", "Due", "Smith", LocalDate.of(1990, 1, 1),
                "john@example.com"
        );

        adminController = new AdminController(adminServiceImpl);
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
    }

    @Test
    void blockCard_ShouldReturnBlockedCard() throws Exception {
        when(adminServiceImpl.blockCard(anyLong(), anyString())).thenReturn(testCard);

        mockMvc.perform(post("/api/v1/admin/cards/block")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCardOperationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNumber").value(testCard.getCardNumber()));

        verify(adminServiceImpl).blockCard(
                userCardOperationRequest.getUserId(),
                userCardOperationRequest.getCardNumber()
        );
    }

    @Test
    void deleteCard_ShouldReturnNoContent() throws Exception {
        Long userId = 1L;
        String cardNumber = "1234";

        doNothing().when(adminServiceImpl).deleteCard(userId, cardNumber);

        mockMvc.perform(delete("/api/v1/admin/cards/delete/{cardNumber}/user/{userId}",
                        cardNumber, userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(adminServiceImpl).deleteCard(userId, cardNumber);
        verifyNoMoreInteractions(adminServiceImpl);
    }

    @Test
    void setBalance_ShouldReturnUpdatedCard() throws Exception {
        when(adminServiceImpl.updateUserBalance(anyLong(), anyString(), any(BigDecimal.class)))
                .thenReturn(testCard);

        mockMvc.perform(post("/api/v1/admin/cards/set_balance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(replenishmentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1000));

        verify(adminServiceImpl).updateUserBalance(1L, "**** **** **** 1234", BigDecimal.valueOf(1000));
    }

    @Test
    void getAllCards_ShouldReturnCardList() throws Exception {
        when(adminServiceImpl.getAllCards()).thenReturn(List.of(testCard));

        mockMvc.perform(get("/api/v1/admin/cards/get_all_info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cardNumber").value(testCard.getCardNumber()))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getUserCards_ShouldReturnUserCards() throws Exception {
        when(adminServiceImpl.getUserCards(anyLong())).thenReturn(List.of(testCard));

        mockMvc.perform(get("/api/v1/admin/cards/get_by_user_id/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cardNumber").value(testCard.getCardNumber()))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    // Users tests
    @Test
    void getAllUsers_ShouldReturnUserList() throws Exception {
        when(adminServiceImpl.getAllUsers()).thenReturn(List.of(testUser));

        mockMvc.perform(get("/api/v1/admin/users/get_all_info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value(testUser.getEmail()))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getUserById_ShouldReturnUser() throws Exception {
        when(adminServiceImpl.getUserById(anyLong())).thenReturn(testUser);

        mockMvc.perform(get("/api/v1/admin/users/get_by_user_id/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(testUser.getEmail()));
    }

    @Test
    void getUserByEmail_ShouldReturnUser() throws Exception {
        when(adminServiceImpl.getUserByEmail(anyString())).thenReturn(testUser);

        mockMvc.perform(get("/api/v1/admin/users/get_by_email/john@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(testUser.getEmail()));
    }

    @Test
    void createUser_ShouldReturnCreatedUser() throws Exception {
        when(adminServiceImpl.createUser(any(UserRegisterRequest.class))).thenReturn(testUser);

        mockMvc.perform(post("/api/v1/admin/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(testUser.getEmail()));
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser() throws Exception {
        when(adminServiceImpl.updateUser(anyLong(), any(UpdateUserRequest.class))).thenReturn(testUser);

        mockMvc.perform(patch("/api/v1/admin/users/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(testUser.getEmail()));
    }

    @Test
    void deleteUser_ShouldReturnNoContent() throws Exception {
        doNothing().when(adminServiceImpl).deleteUser(anyLong());

        mockMvc.perform(delete("/api/v1/admin/users/delete/1"))
                .andExpect(status().isNoContent());

        verify(adminServiceImpl).deleteUser(1L);
    }
}