package com.example.bankcards.service;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.dto.UserDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.cards.CardNotFoundException;
import com.example.bankcards.exception.cards.CardOperationException;
import com.example.bankcards.exception.users.EmailBusyException;
import com.example.bankcards.exception.users.PhoneNumberBusyException;
import com.example.bankcards.exception.users.UserNotFoundException;
import com.example.bankcards.repository.CardBaseRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardNumberGenerator;
import com.example.bankcards.util.Mapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final CardRepository cardRepository;
    private final CardBaseRepository cardBaseRepository;
    private final UserRepository userRepository;
    private final CardNumberGenerator cardNumberGenerator;
    private final Mapper mapper;

    //Более интересная логика и приближена к реальности
//    public List<String> createCard() {return cardNumberGenerator.generateBatch();}
//
//    public CardDTO activateCard(String email) {
//        var user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new UserNotFoundException("User not found"));
//
//        String actualNumber = cardBaseRepository.findRandomCard()
//                .orElseThrow(() -> new CardNotFoundInBaseException("No cards in base!"));
//
//        var card = new Card();
//        card.setCardHolder(user.getName() + " " + user.getSurname());
//        card.setCardNumber(actualNumber);
//        card.setExpiryDate(LocalDate.now().plusYears(5));
//        card.setStatus(CardStatus.ACTIVE);
//        card.setUser(user);
//
//        cardBaseRepository.deleteByCardNumber(actualNumber);
//        cardRepository.save(card);
//        return mapper.toCardDTO(card);
//    }

    //======================================

    public CardDTO createCard(Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        var card = new Card();
        card.setCardHolder(user.getName() + " " + user.getSurname());
        card.setCardNumber(cardNumberGenerator.generateNumber());
        card.setExpiryDate(LocalDate.now().plusYears(5));
        card.setStatus(CardStatus.ACTIVE);
        card.setUser(user);

        cardRepository.save(card);
        return mapper.toCardDTO(card);
    }

    @Transactional
    public CardDTO setActiveStatus(Long userId, String cardNumber) {
        var user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Card card = cardRepository.findByCardNumberAndUser(cardNumber, user)
                .orElseThrow(() -> new CardNotFoundException(cardNumber, user.getEmail()));

        if (card.getStatus() != CardStatus.ACTIVE) {
            card.setStatus(CardStatus.ACTIVE);
            cardRepository.save(card);
            return mapper.toCardDTO(card);
        } else {
            throw new CardOperationException("Card is active!");
        }
    }

    @Transactional
    public CardDTO blockCard(Long userId, String cardNumber) {
        var user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Card card = cardRepository.findByCardNumberAndUser(cardNumber, user)
                .orElseThrow(() -> new CardNotFoundException(cardNumber, user.getEmail()));

        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new CardOperationException(
                    "Cannot deactivate card. Current status: " + card.getStatus());
        }
        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
        return mapper.toCardDTO(card);
    }

    @Transactional
    public void deleteCard(Long userId, String cardNumber) {
        var user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Card card = cardRepository.findByCardNumberAndUser(cardNumber, user)
                .orElseThrow(() -> new CardNotFoundException(cardNumber, user.getEmail()));

        cardRepository.delete(card);
    }

    public List<CardDTO> getAllCards() {
        return cardRepository.findAll().stream()
                .map(mapper::toCardDTO)
                .collect(Collectors.toList());
    }

    public List<CardDTO> getUserCards(Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        return cardRepository.findByUser(user).stream()
                .map(mapper::toCardDTO)
                .collect(Collectors.toList());
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        return mapper.toDTO(user);
    }

    public UserDTO getUserByEmail(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
        return mapper.toDTO(user);
    }

    public UserDTO getUserByPhone(String phone) {
        var user = userRepository.findByPhoneNumber(phone)
                .orElseThrow(UserNotFoundException::new);
        return mapper.toDTO(user);
    }

    public UserDTO createUser(UserDTO userDTO) {
        var user = new User();
        user.setName(userDTO.getName());
        user.setSurname(userDTO.getSurname());
        user.setPatronymic(userDTO.getPatronymic());
        user.setBirthday(userDTO.getBirthday());
        user.setEmail(userDTO.getEmail());
        user.setPhoneNumber(userDTO.getPhoneNumber());

        User savedUser = userRepository.save(user);
        return mapper.toDTO(savedUser);
    }

    public UserDTO updateUser(Long userId, UserDTO userDTO) {
        var existedUser = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        String currentRefreshToken = existedUser.getRefreshToken();
        LocalDateTime currentRefreshTokenExpiry = existedUser.getRefreshTokenExpiry();

        if (userDTO.getName() != null) {
            existedUser.setName(userDTO.getName());
        }
        if (userDTO.getSurname() != null) {
            existedUser.setSurname(userDTO.getSurname());
        }
        if (userDTO.getPatronymic() != null) {
            existedUser.setPatronymic(userDTO.getPatronymic());
        }
        if (userDTO.getBirthday() != null) {
            existedUser.setBirthday(userDTO.getBirthday());
        }
        if (userDTO.getEmail() != null && !userDTO.getEmail().equals(existedUser.getEmail())) {
            if (userRepository.existsByEmail(userDTO.getEmail())) {
                throw new EmailBusyException(userDTO.getEmail());
            }
            existedUser.setEmail(userDTO.getEmail());
        }
        if (userDTO.getPhoneNumber() != null && !userDTO.getPhoneNumber().equals(existedUser.getPhoneNumber())) {
            if (userRepository.existsByEmail(userDTO.getPhoneNumber())) {
                throw new PhoneNumberBusyException(userDTO.getPhoneNumber());
            }
            existedUser.setPhoneNumber(userDTO.getPhoneNumber());
        }
        existedUser.setRefreshToken(currentRefreshToken);
        existedUser.setRefreshTokenExpiry(currentRefreshTokenExpiry);

        userRepository.save(existedUser);
        return mapper.toDTO(existedUser);
    }

    @Transactional
    public void deleteUser(Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        userRepository.delete(user);
    }
}
