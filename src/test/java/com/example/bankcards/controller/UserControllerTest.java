package com.example.bankcards.controller;

import com.example.bankcards.config.SecurityConfig;
import com.example.bankcards.dto.cards.CardDTO;
import com.example.bankcards.dto.cards.TransferRequest;
import com.example.bankcards.dto.cards.TransferResponse;
import com.example.bankcards.dto.users.UserDTO;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.GlobalExceptionHandler;
import com.example.bankcards.exception.cards.CardOperationException;
import com.example.bankcards.service.cards.CardServiceImpl;
import com.example.bankcards.service.transfers.TransferServiceImpl;
import com.example.bankcards.service.users.UserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Mock
    private UserServiceImpl userServiceImpl;
    @Mock
    private CardServiceImpl cardServiceImpl;
    @Mock
    private TransferServiceImpl transferServiceImpl;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private final String testCardNumber = "1234";
    private final BigDecimal testAmount = BigDecimal.valueOf(100);
    private CardDTO testCard;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        UserController controller = new UserController(userServiceImpl, cardServiceImpl, transferServiceImpl);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));

        testCard = new CardDTO(
                "**** **** **** " + testCardNumber,
                LocalDate.now().plusYears(3),
                "John Doe",
                BigDecimal.valueOf(1000),
                CardStatus.ACTIVE
        );

        userDTO = new UserDTO(
                1L,
                "John",
                "Doe",
                null,
                LocalDate.of(1990, 1, 1),
                "user@example.com",
                LocalDateTime.now()
        );

        User testUser = User.builder()
                .userId(1L)
                .name("John")
                .surname("Doe")
                .patronymic("Smith")
                .birthday(LocalDate.of(1990, 1, 1))
                .email("testUser@example.com")
                .password("password")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        testUser,
                        null,
                        testUser.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(
                        new AuthenticationPrincipalArgumentResolver(),
                        new PageableHandlerMethodArgumentResolver()
                )
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @WithUserDetails(value = "user@example.com")
    void simplifiedTest() throws Exception {
        mockMvc.perform(get("/api/v1/user/cards"))
                .andExpect(status().isOk());
    }

    @Test
    void getUserCards_ShouldReturnCardsPage() throws Exception {
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("expiryDate").descending());
        Page<CardDTO> page = new PageImpl<>(List.of(testCard), pageRequest, 1);

        when(cardServiceImpl.getUserCards(eq(1L), isNull(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/user/cards")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "expiryDate,desc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].cardNumber").value(testCard.getCardNumber()))
                .andExpect(jsonPath("$.content[0].expiryDate").value(testCard.getExpiryDate().toString()))
                .andExpect(jsonPath("$.content[0].cardHolder").value(testCard.getCardHolder()))
                .andExpect(jsonPath("$.content[0].balance").value(testCard.getBalance().doubleValue()))
                .andExpect(jsonPath("$.content[0].status").value(testCard.getStatus().name()));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void getCardBalance_ShouldReturnBalance() throws Exception {
        when(cardServiceImpl.getCardBalance(eq(1L), eq(testCardNumber)))
                .thenReturn(BigDecimal.valueOf(1000));

        mockMvc.perform(get("/api/v1/user/cards/{cardNumber}/balance", testCardNumber))
                .andExpect(status().isOk())
                .andExpect(content().string(BigDecimal.valueOf(1000).toString()));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void blockCard_ShouldReturnOk() throws Exception {
        doNothing().when(cardServiceImpl).blockCard(eq(1L), eq(testCardNumber));

        mockMvc.perform(post("/api/v1/user/cards/{cardNumber}/block", testCardNumber))
                .andExpect(status().isOk());

        verify(cardServiceImpl).blockCard(1L, testCardNumber);
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void transferBetweenCards_ShouldReturnTransferResponse() throws Exception {
        String testToCardNumber = "5678";
        TransferRequest request = new TransferRequest(testCardNumber, testToCardNumber, testAmount);
        TransferResponse response = new TransferResponse(
                testCard,
                new CardDTO("**** **** **** " + testToCardNumber,
                        LocalDate.now().plusYears(2),
                        "Recipient",
                        BigDecimal.valueOf(500),
                        CardStatus.ACTIVE)
        );

        when(transferServiceImpl.transferBetweenUserCards(
                eq(1L),
                eq(testCardNumber),
                eq(testToCardNumber),
                eq(testAmount)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/user/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromCard.cardNumber").value("**** **** **** " + testCardNumber))
                .andExpect(jsonPath("$.toCard.cardNumber").value("**** **** **** " + testToCardNumber));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void getUserProfile_ShouldReturnUserProfile() throws Exception {
        when(userServiceImpl.getUserProfile(eq(1L))).thenReturn(userDTO);

        mockMvc.perform(get("/api/v1/user/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDTO.getId()))
                .andExpect(jsonPath("$.name").value(userDTO.getName()))
                .andExpect(jsonPath("$.surname").value(userDTO.getSurname()))
                .andExpect(jsonPath("$.email").value(userDTO.getEmail()));
    }


    @Test
    @WithMockUser(username = "user@example.com")
    void transferBetweenCards_ShouldReturnErrorWhenInsufficientFunds() throws Exception {
        TransferRequest request = new TransferRequest("1234", "5678", testAmount);

        when(transferServiceImpl.transferBetweenUserCards(any(), any(), any(), any()))
                .thenThrow(new CardOperationException("Not enough funds"));

        mockMvc.perform(post("/api/v1/user/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Not enough funds"));
    }
}