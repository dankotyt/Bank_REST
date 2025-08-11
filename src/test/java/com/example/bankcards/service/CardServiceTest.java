package com.example.bankcards.service;

import com.example.bankcards.dto.cards.CardDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.cards.CardNotFoundException;
import com.example.bankcards.exception.cards.CardOperationException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.cards.CardServiceImpl;
import com.example.bankcards.util.Mapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private Mapper mapper;
    @InjectMocks
    private CardServiceImpl cardService;

    private Long testUserId;
    private String testSearch;
    private Pageable testPageable;
    private User testUser;
    private Card testCard;
    private CardDTO testCardDto;
    private String testCardNumber;
    private String testFullCardNumber;

    @BeforeEach
    void setUp() {
        testUserId = 1L;
        testSearch = "test";
        testPageable = PageRequest.of(0, 10);
        testCardNumber = "1234";
        testFullCardNumber = "**** **** **** " + testCardNumber;

        testUser = new User(
                testUserId, "John", "Doe", "Smith",
                LocalDate.of(1990, 12, 10), "john@example.com",
                "password", Role.USER,
                LocalDateTime.now(), null, null
        );

        testCard = new Card();
        testCard.setCardNumber(testFullCardNumber);
        testCard.setUser(testUser);
        testCard.setBalance(new BigDecimal("1000.00"));
        testCard.setStatus(CardStatus.ACTIVE);

        testCardDto = new CardDTO();
        testCardDto.setCardNumber(testFullCardNumber);
    }

    @Test
    void getUserCards_ShouldReturnFilteredPage() {
        when(cardRepository.findAll(any(Specification.class), eq(testPageable)))
                .thenReturn(new PageImpl<>(List.of(testCard)));
        when(mapper.toCardDTO(testCard)).thenReturn(testCardDto);

        Page<CardDTO> result = cardService.getUserCards(testUserId, testSearch, testPageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst()).isEqualTo(testCardDto);
        verify(cardRepository).findAll(any(Specification.class), eq(testPageable));
    }

    @Test
    void getUserCards_WithNullSearch_ShouldReturnAllCards() {
        when(cardRepository.findAll(any(Specification.class), eq(testPageable)))
                .thenReturn(new PageImpl<>(List.of()));

        cardService.getUserCards(testUserId, null, testPageable);

        verify(cardRepository).findAll(any(Specification.class), eq(testPageable));
    }

    @Test
    void blockCard_shouldBlockActiveCard() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndUser_UserId(testFullCardNumber, testUserId))
                .thenReturn(Optional.of(testCard));

        cardService.blockCard(testUserId, testCardNumber);

        assertEquals(CardStatus.BLOCKED, testCard.getStatus());
        verify(cardRepository).save(testCard);
    }

    @Test
    void blockCard_whenCardAlreadyBlocked_shouldThrowException() {
        testCard.setStatus(CardStatus.BLOCKED);

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndUser_UserId(testFullCardNumber, testUserId))
                .thenReturn(Optional.of(testCard));

        assertThrows(CardOperationException.class,
                () -> cardService.blockCard(testUserId, testCardNumber));
    }

    @Test
    void blockCard_whenCardNotFound_shouldThrowException() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndUser_UserId(testFullCardNumber, testUserId))
                .thenReturn(Optional.empty());

        CardNotFoundException exception = assertThrows(CardNotFoundException.class,
                () -> cardService.blockCard(testUserId, testCardNumber));

        String expectedMessage = "Card not found with number " + testFullCardNumber +
                " for user with email " + testUser.getEmail();
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void getCardBalance_shouldReturnBalance() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndUser_UserId(testFullCardNumber, testUserId))
                .thenReturn(Optional.of(testCard));

        BigDecimal balance = cardService.getCardBalance(testUserId, testCardNumber);

        assertEquals(new BigDecimal("1000.00"), balance);
    }

    @Test
    void getCardBalance_whenCardNotFound_shouldThrowException() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndUser_UserId(testFullCardNumber, testUserId))
                .thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class,
                () -> cardService.getCardBalance(testUserId, testCardNumber));
    }
}