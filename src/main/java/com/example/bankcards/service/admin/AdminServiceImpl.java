package com.example.bankcards.service.admin;

import com.example.bankcards.dto.cards.CardDTO;
import com.example.bankcards.dto.users.UpdateUserRequest;
import com.example.bankcards.dto.users.UserDTO;
import com.example.bankcards.dto.users.UserRegisterRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.cards.CardNotFoundException;
import com.example.bankcards.exception.cards.CardOperationException;
import com.example.bankcards.exception.users.EmailBusyException;
import com.example.bankcards.exception.users.UserExistsException;
import com.example.bankcards.exception.users.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.Mapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    //todo создать метод генерации
    private final CardNumberGenerator cardNumberGenerator;
    private final Mapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public CardDTO createCard(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        Card card = new Card();
        card.setCardHolder(user.getName() + " " + user.getSurname());
        //todo создать метод генерации
        card.setCardNumber(cardNumberGenerator.generateNumber());
        card.setExpiryDate(LocalDate.now().plusYears(5));
        card.setStatus(CardStatus.ACTIVE);
        card.setUser(user);

        cardRepository.save(card);
        return mapper.toCardDTO(card);
    }

    @Override
    @Transactional
    public CardDTO activateCard(Long userId, String cardNumber) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        //чтобы вводить последние 4 цифры
        String formatCardNumber = "**** **** **** " + cardNumber;
        Card card = cardRepository.findByCardNumberAndUser(formatCardNumber, user)
                .orElseThrow(() -> new CardNotFoundException(cardNumber, user.getEmail()));

        if (card.getStatus() != CardStatus.ACTIVE) {
            card.setStatus(CardStatus.ACTIVE);
            cardRepository.save(card);
            return mapper.toCardDTO(card);
        } else {
            throw new CardOperationException("Card is active!");
        }
    }

    @Override
    @Transactional
    public CardDTO blockCard(Long userId, String cardNumber) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        String formatCardNumber = "**** **** **** " + cardNumber;
        Card card = cardRepository.findByCardNumberAndUser(formatCardNumber, user)
                .orElseThrow(() -> new CardNotFoundException(cardNumber, user.getEmail()));

        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new CardOperationException(
                    "Cannot deactivate card. Current status: " + card.getStatus());
        }
        card.setStatus(CardStatus.BLOCKED);
        return mapper.toCardDTO(card);
    }

    @Override
    @Transactional
    public void deleteCard(Long userId, String cardNumber) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        String formatCardNumber = "**** **** **** " + cardNumber;

        Card card = cardRepository.findByCardNumberAndUser(formatCardNumber, user)
                .orElseThrow(() -> new CardNotFoundException(cardNumber, user.getEmail()));

        cardRepository.delete(card);
    }

    @Override
    @Transactional
    public CardDTO updateUserBalance(Long userId, String cardNumber, BigDecimal balance) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        String formatCardNumber = "**** **** **** " + cardNumber;
        Card card = cardRepository.findByCardNumberAndUser(formatCardNumber, user)
                .orElseThrow(() -> new CardNotFoundException(cardNumber, user.getEmail()));
        card.setBalance(balance);
        return mapper.toCardDTO(card);
    }

    @Override
    public List<CardDTO> getAllCards() {
        return cardRepository.findAll().stream()
                .map(mapper::toCardDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CardDTO> getUserCards(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        return cardRepository.findByUser(user).stream()
                .map(mapper::toCardDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        return mapper.toDTO(user);
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
        return mapper.toDTO(user);
    }

    @Override
    public UserDTO createUser(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserExistsException("User with this email or phone number already exists!");
        }
        var user = new User();
        user.setName(request.getName());
        user.setSurname(request.getSurname());
        user.setPatronymic(request.getPatronymic());
        user.setBirthday(request.getBirthday());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);
        return mapper.toDTO(savedUser);
    }

    @Override
    public UserDTO updateUser(Long userId, UpdateUserRequest request) {
        User existedUser = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        String currentRefreshToken = existedUser.getRefreshToken();
        LocalDateTime currentRefreshTokenExpiry = existedUser.getRefreshTokenExpiry();

        if (request.getName() != null) {
            existedUser.setName(request.getName());
        }
        if (request.getSurname() != null) {
            existedUser.setSurname(request.getSurname());
        }
        if (request.getPatronymic() != null) {
            existedUser.setPatronymic(request.getPatronymic());
        }
        if (request.getBirthday() != null) {
            existedUser.setBirthday(request.getBirthday());
        }
        if (request.getEmail() != null && !request.getEmail().equals(existedUser.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new EmailBusyException(request.getEmail());
            }
            existedUser.setEmail(request.getEmail());
        }

        existedUser.setRefreshToken(currentRefreshToken);
        existedUser.setRefreshTokenExpiry(currentRefreshTokenExpiry);

        userRepository.save(existedUser);
        return mapper.toDTO(existedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        userRepository.delete(user);
    }
}
