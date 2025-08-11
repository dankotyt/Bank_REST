package com.example.bankcards.service;

import com.example.bankcards.dto.cards.CardDTO;
import com.example.bankcards.dto.users.UpdateUserRequest;
import com.example.bankcards.dto.users.UserDTO;
import com.example.bankcards.dto.users.UserRegisterRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.cards.CardOperationException;
import com.example.bankcards.exception.users.EmailBusyException;
import com.example.bankcards.exception.users.UserExistsException;
import com.example.bankcards.exception.users.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.admin.AdminServiceImpl;
import com.example.bankcards.util.Mapper;
import com.example.bankcards.util.generatorNumbers.CardNumberGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardNumberGenerator cardNumberGenerator;

    @Mock
    private Mapper mapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminServiceImpl adminService;

    private User testUser;
    private Card testCard;
    private UserDTO userDTO;
    private CardDTO cardDTO;
    private UserRegisterRequest registerRequest;
    private UpdateUserRequest updateRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setName("John");
        testUser.setSurname("Doe");
        testUser.setEmail("john@example.com");

        testCard = new Card();
        testCard.setCardId(1L);
        testCard.setCardNumber("1234567890123456");
        testCard.setUser(testUser);
        testCard.setStatus(CardStatus.ACTIVE);
        testCard.setBalance(BigDecimal.ZERO);

        userDTO = new UserDTO(
                1L, "John", "Doe", null,
                LocalDate.of(1990, 1, 1), "john@example.com",
                LocalDateTime.now()
        );

        cardDTO = new CardDTO(
                "1234567890123456", LocalDate.now().plusYears(3),
                "John Doe", BigDecimal.ZERO, CardStatus.ACTIVE

        );

        registerRequest = new UserRegisterRequest(
                "John", "Doe", null, LocalDate.of(1990, 1, 1),
                "john@example.com", "password123", Role.USER
        );

        updateRequest = new UpdateUserRequest(
                "Johny", "Doe", null, LocalDate.of(1990, 1, 1),
                "johny@example.com"
        );
    }

    //Cards
    @Test
    void createCard_ShouldCreateNewCard() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cardNumberGenerator.generateNumber()).thenReturn("1234567890123456");
        when(mapper.toCardDTO(any(Card.class))).thenReturn(cardDTO);

        CardDTO result = adminService.createCard(1L);

        assertThat(result).isEqualTo(cardDTO);
        verify(cardRepository).save(any(Card.class));
        verify(cardNumberGenerator).generateNumber();
    }

    @Test
    void createCard_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.createCard(1L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void setActiveStatus_ShouldActivateCard() {
        String cardNumber = "3456";
        String formattedNumber = "**** **** **** 3456";
        testCard.setStatus(CardStatus.BLOCKED);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndUser(formattedNumber, testUser))
                .thenReturn(Optional.of(testCard));
        when(mapper.toCardDTO(testCard)).thenReturn(cardDTO);

        CardDTO result = adminService.activateCard(1L, cardNumber);

        assertThat(result).isEqualTo(cardDTO);
        assertThat(testCard.getStatus()).isEqualTo(CardStatus.ACTIVE);
        verify(cardRepository).save(testCard);
    }

    @Test
    void setActiveStatus_WhenCardAlreadyActive_ShouldThrowException() {
        String cardNumber = "3456";
        String formattedNumber = "**** **** **** 3456";

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndUser(formattedNumber, testUser))
                .thenReturn(Optional.of(testCard));

        assertThatThrownBy(() -> adminService.activateCard(1L, cardNumber))
                .isInstanceOf(CardOperationException.class)
                .hasMessage("Card is active!");
    }

    @Test
    void blockCard_ShouldBlockActiveCard() {
        String cardNumber = "3456";
        String formattedNumber = "**** **** **** 3456";

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndUser(formattedNumber, testUser))
                .thenReturn(Optional.of(testCard));
        when(mapper.toCardDTO(testCard)).thenReturn(cardDTO);

        CardDTO result = adminService.blockCard(1L, cardNumber);

        assertThat(result).isEqualTo(cardDTO);
        assertThat(testCard.getStatus()).isEqualTo(CardStatus.BLOCKED);
    }

    @Test
    void blockCard_WhenCardNotActive_ShouldThrowException() {
        String cardNumber = "3456";
        String formattedNumber = "**** **** **** 3456";
        testCard.setStatus(CardStatus.BLOCKED);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndUser(formattedNumber, testUser))
                .thenReturn(Optional.of(testCard));

        assertThatThrownBy(() -> adminService.blockCard(1L, cardNumber))
                .isInstanceOf(CardOperationException.class)
                .hasMessageContaining("Cannot deactivate card");
    }

    @Test
    void deleteCard_ShouldDeleteCard() {
        String cardNumber = "3456";
        String formattedNumber = "**** **** **** 3456";

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndUser(formattedNumber, testUser))
                .thenReturn(Optional.of(testCard));

        adminService.deleteCard(1L, cardNumber);

        verify(cardRepository).delete(testCard);
    }

    @Test
    void getAllCards_ShouldReturnAllCards() {
        Card card1 = new Card();
        card1.setCardNumber("1111222233334444");
        Card card2 = new Card();
        card2.setCardNumber("5555666677778888");

        CardDTO cardDTO1 = new CardDTO("1111222233334444", LocalDate.now().plusYears(3),
                "John Doe", BigDecimal.ZERO, CardStatus.ACTIVE);
        CardDTO cardDTO2 = new CardDTO("5555666677778888", LocalDate.now().plusYears(3),
                "Max Doe", BigDecimal.ZERO, CardStatus.ACTIVE);

        when(cardRepository.findAll()).thenReturn(List.of(card1, card2));
        when(mapper.toCardDTO(card1)).thenReturn(cardDTO1);
        when(mapper.toCardDTO(card2)).thenReturn(cardDTO2);

        List<CardDTO> result = adminService.getAllCards();

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(cardDTO1, cardDTO2);
        verify(cardRepository).findAll();
    }


    @Test
    void updateUserBalance_ShouldUpdateBalance() {
        String cardNumber = "3456";
        String formattedNumber = "**** **** **** 3456";
        BigDecimal newBalance = new BigDecimal("1000.00");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cardRepository.findByCardNumberAndUser(formattedNumber, testUser))
                .thenReturn(Optional.of(testCard));
        when(mapper.toCardDTO(testCard)).thenReturn(cardDTO);

        CardDTO result = adminService.updateUserBalance(1L, cardNumber, newBalance);

        assertThat(result).isEqualTo(cardDTO);
        assertThat(testCard.getBalance()).isEqualByComparingTo(newBalance);
    }

    @Test
    void getUserCards_ShouldReturnUserCards() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cardRepository.findByUser(testUser)).thenReturn(List.of(testCard));
        when(mapper.toCardDTO(testCard)).thenReturn(cardDTO);

        List<CardDTO> result = adminService.getUserCards(1L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(cardDTO);
    }

    //Users
    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(testUser));
        when(mapper.toDTO(testUser)).thenReturn(userDTO);

        List<UserDTO> result = adminService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(userDTO);
    }

    @Test
    void getUserById_ShouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(mapper.toDTO(testUser)).thenReturn(userDTO);

        UserDTO result = adminService.getUserById(1L);

        assertThat(result).isEqualTo(userDTO);
    }

    @Test
    void getUserByEmail_ShouldReturnUser() {
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(mapper.toDTO(testUser)).thenReturn(userDTO);

        UserDTO result = adminService.getUserByEmail(email);

        assertThat(result).isEqualTo(userDTO);
        verify(userRepository).findByEmail(email);
    }

    @Test
    void getUserByEmail_WhenUserNotFound_ShouldThrowException() {
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.getUserByEmail(email))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void createUser_ShouldCreateNewUser() {
        when(userRepository.existsByEmail("john@example.com"))
                .thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(mapper.toDTO(testUser)).thenReturn(userDTO);

        UserDTO result = adminService.createUser(registerRequest);

        assertThat(result).isEqualTo(userDTO);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_WhenEmailExists_ShouldThrowException() {
        when(userRepository.existsByEmail("john@example.com"))
                .thenReturn(true);

        assertThatThrownBy(() -> adminService.createUser(registerRequest))
                .isInstanceOf(UserExistsException.class);
    }

    @Test
    void updateUser_ShouldUpdateUserData() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(mapper.toDTO(testUser)).thenReturn(userDTO);

        UserDTO result = adminService.updateUser(1L, updateRequest);

        assertThat(result).isEqualTo(userDTO);
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUser_WhenEmailExists_ShouldThrowException() {
        updateRequest.setEmail("new@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        assertThatThrownBy(() -> adminService.updateUser(1L, updateRequest))
                .isInstanceOf(EmailBusyException.class);
    }

    @Test
    void updateUser_ShouldUpdatePatronymic() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setPatronymic("NewPatronymic");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(mapper.toDTO(testUser)).thenReturn(userDTO);

        UserDTO result = adminService.updateUser(1L, request);

        assertThat(result).isEqualTo(userDTO);
        assertThat(testUser.getPatronymic()).isEqualTo("NewPatronymic");
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUser_ShouldNotUpdateFieldsWhenNull() {
        UpdateUserRequest request = new UpdateUserRequest();

        User originalUser = new User();
        originalUser.setUserId(1L);
        originalUser.setName("Original");
        originalUser.setSurname("OriginalSurname");
        originalUser.setPatronymic("OriginalPatronymic");
        originalUser.setBirthday(LocalDate.of(1980, 1, 1));
        originalUser.setEmail("original@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(originalUser));
        when(mapper.toDTO(originalUser)).thenReturn(userDTO);

        UserDTO result = adminService.updateUser(1L, request);

        assertThat(result).isEqualTo(userDTO);
        assertThat(originalUser.getName()).isEqualTo("Original");
        assertThat(originalUser.getSurname()).isEqualTo("OriginalSurname");
        assertThat(originalUser.getPatronymic()).isEqualTo("OriginalPatronymic");
        verify(userRepository).save(originalUser);
    }

    @Test
    void deleteUser_ShouldDeleteUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        adminService.deleteUser(1L);

        verify(userRepository).delete(testUser);
    }
}