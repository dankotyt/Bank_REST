package com.example.bankcards.service;

import com.example.bankcards.dto.cards.CardDTO;
import com.example.bankcards.dto.cards.TransferResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.cards.CardNotFoundException;
import com.example.bankcards.exception.cards.CardOperationException;
import com.example.bankcards.exception.users.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.transfers.TransferServiceImpl;
import com.example.bankcards.util.Mapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Mapper mapper;

    @InjectMocks
    private TransferServiceImpl transferService;

    private final Long testUserId = 1L;
    private final String testFromCardNumber = "1234";
    private final String testToCardNumber = "5678";
    private final BigDecimal testAmount = new BigDecimal("100.00");
    private final String formattedFromCard = "**** **** **** " + testFromCardNumber;
    private final String formattedToCard = "**** **** **** " + testToCardNumber;

    private User testUser;
    private Card fromCard;
    private Card toCard;
    private CardDTO fromCardDto;
    private CardDTO toCardDto;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(testUserId)
                .email("test@example.com")
                .build();

        fromCard = Card.builder()
                .cardNumber(formattedFromCard)
                .cardHolder("John Doe")
                .expiryDate(LocalDate.now().plusYears(2))
                .balance(new BigDecimal("500.00"))
                .status(CardStatus.ACTIVE)
                .user(testUser)
                .build();

        toCard = Card.builder()
                .cardNumber(formattedToCard)
                .cardHolder("Jane Smith")
                .expiryDate(LocalDate.now().plusYears(3))
                .balance(new BigDecimal("200.00"))
                .status(CardStatus.ACTIVE)
                .user(testUser)
                .build();

        fromCardDto = new CardDTO();
        toCardDto = new CardDTO();
    }

    @Test
    void transferBetweenUserCards_ShouldTransferSuccessfully() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndUser(formattedFromCard, testUser))
                .thenReturn(Optional.of(fromCard));
        when(cardRepository.findByCardNumberAndUser(formattedToCard, testUser))
                .thenReturn(Optional.of(toCard));
        when(mapper.toCardDTO(fromCard)).thenReturn(fromCardDto);
        when(mapper.toCardDTO(toCard)).thenReturn(toCardDto);

        TransferResponse response = transferService.transferBetweenUserCards(
                testUserId, testFromCardNumber, testToCardNumber, testAmount);

        assertThat(response.getFromCard()).isEqualTo(fromCardDto);
        assertThat(response.getToCard()).isEqualTo(toCardDto);
        assertThat(fromCard.getBalance()).isEqualByComparingTo("400.00");
        assertThat(toCard.getBalance()).isEqualByComparingTo("300.00");
        verify(cardRepository).saveAll(List.of(fromCard, toCard));
    }

    @Test
    void transferBetweenUserCards_WhenNegativeAmount_ShouldThrowException() {
        assertThatThrownBy(() -> transferService.transferBetweenUserCards(
                testUserId, testFromCardNumber, testToCardNumber, new BigDecimal("-100")))
                .isInstanceOf(CardOperationException.class)
                .hasMessage("Amount must be positive");
    }

    @Test
    void transferBetweenUserCards_WhenZeroAmount_ShouldThrowException() {
        assertThatThrownBy(() -> transferService.transferBetweenUserCards(
                testUserId, testFromCardNumber, testToCardNumber, BigDecimal.ZERO))
                .isInstanceOf(CardOperationException.class)
                .hasMessage("Amount must be positive");
    }

    @Test
    void transferBetweenUserCards_WhenFromCardBlocked_ShouldThrowException() {
        fromCard.setStatus(CardStatus.BLOCKED);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndUser(formattedFromCard, testUser))
                .thenReturn(Optional.of(fromCard));
        when(cardRepository.findByCardNumberAndUser(formattedToCard, testUser))
                .thenReturn(Optional.of(toCard));

        assertThatThrownBy(() -> transferService.transferBetweenUserCards(
                testUserId, testFromCardNumber, testToCardNumber, testAmount))
                .isInstanceOf(CardOperationException.class)
                .hasMessage("One of the cards is not active");
    }

    @Test
    void transferBetweenUserCards_WhenToCardBlocked_ShouldThrowException() {
        toCard.setStatus(CardStatus.BLOCKED);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndUser(formattedFromCard, testUser))
                .thenReturn(Optional.of(fromCard));
        when(cardRepository.findByCardNumberAndUser(formattedToCard, testUser))
                .thenReturn(Optional.of(toCard));

        assertThatThrownBy(() -> transferService.transferBetweenUserCards(
                testUserId, testFromCardNumber, testToCardNumber, testAmount))
                .isInstanceOf(CardOperationException.class)
                .hasMessage("One of the cards is not active");
    }

    @Test
    void transferBetweenUserCards_WhenInsufficientFunds_ShouldThrowException() {
        BigDecimal largeAmount = new BigDecimal("1000.00");
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndUser(formattedFromCard, testUser))
                .thenReturn(Optional.of(fromCard));
        when(cardRepository.findByCardNumberAndUser(formattedToCard, testUser))
                .thenReturn(Optional.of(toCard));

        assertThatThrownBy(() -> transferService.transferBetweenUserCards(
                testUserId, testFromCardNumber, testToCardNumber, largeAmount))
                .isInstanceOf(CardOperationException.class)
                .hasMessage("Insufficient funds");
    }

    @Test
    void transferBetweenUserCards_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.transferBetweenUserCards(
                testUserId, testFromCardNumber, testToCardNumber, testAmount))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void transferBetweenUserCards_WhenFromCardNotFound_ShouldThrowException() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndUser(formattedFromCard, testUser))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.transferBetweenUserCards(
                testUserId, testFromCardNumber, testToCardNumber, testAmount))
                .isInstanceOf(CardNotFoundException.class)
                .hasMessageContaining("Card not found with number");
    }

    @Test
    void transferBetweenUserCards_WhenToCardNotFound_ShouldThrowException() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndUser(formattedFromCard, testUser))
                .thenReturn(Optional.of(fromCard));
        when(cardRepository.findByCardNumberAndUser(formattedToCard, testUser))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.transferBetweenUserCards(
                testUserId, testFromCardNumber, testToCardNumber, testAmount))
                .isInstanceOf(CardNotFoundException.class)
                .hasMessageContaining("Card not found with number");
    }

    @Test
    void transferBetweenUserCards_ShouldUpdateBalancesCorrectly() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndUser(formattedFromCard, testUser))
                .thenReturn(Optional.of(fromCard));
        when(cardRepository.findByCardNumberAndUser(formattedToCard, testUser))
                .thenReturn(Optional.of(toCard));
        when(mapper.toCardDTO(fromCard)).thenReturn(fromCardDto);
        when(mapper.toCardDTO(toCard)).thenReturn(toCardDto);

        TransferResponse response = transferService.transferBetweenUserCards(
                testUserId, testFromCardNumber, testToCardNumber, testAmount);
        TransferResponse expectedResponse = new TransferResponse(fromCardDto, toCardDto);
        assertThat(response).isEqualTo(expectedResponse);

        assertThat(fromCard.getBalance())
                .isEqualByComparingTo("400.00");
        assertThat(toCard.getBalance())
                .isEqualByComparingTo("300.00");
        verify(cardRepository).saveAll(List.of(fromCard, toCard));
    }
}