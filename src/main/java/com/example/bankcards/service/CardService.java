package com.example.bankcards.service;

import com.example.bankcards.dto.cards.CardDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.cards.CardNotFoundException;
import com.example.bankcards.exception.cards.CardOperationException;
import com.example.bankcards.exception.users.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.Mapper;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CardService {
//    private final CardRepository cardRepository;
//    private final CardBaseRepository cardBaseRepository;
//    private final UserRepository userRepository;
//    private final CardNumberGenerator cardNumberGenerator;
//    private final UserMapper userMapper;
//
//    public CardDTO activateCard(Long userId) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new UserNotFoundException("User not found"));
//        var card = new Card();
//        card.setCardHolder(user.getName() + " " + user.getSurname());
//        String actualNumber = cardBaseRepository.findRandomCard()
//                .orElseThrow(() -> new CardNotFoundInBaseException("No cards in base!"));
//        card.setCardNumber(actualNumber);
//        card.setExpiryDate(LocalDate.now().plusYears(5));
//        cardBaseRepository.deleteByCardNumber(actualNumber);
//        cardRepository.save(card);
//        return new CardDTO(
//                card.getCardNumber(),
//                card.getExpiryDate(),
//                card.getCardHolder(),
//                card.getBalance()
//        );
//    }

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final Mapper mapper;

    public Page<CardDTO> getUserCards(Long userId, String search, Pageable pageable) {
        Specification<Card> spec = createCardSpecification(userId, search);
        return cardRepository.findAll(spec, pageable).map(mapper::toCardDTO);
    }

    public void blockCard(Long userId, String cardNumber) {
        Card card = findUserCard(userId, cardNumber);
        validateCardBlock(card);
        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }

    public BigDecimal getCardBalance(Long userId, String cardNumber) {
        return findUserCard(userId, cardNumber).getBalance();
    }

    private Specification<Card> createCardSpecification(Long userId, String search) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));

            if (StringUtils.hasText(search)) {
                String searchTerm = "%" + search.toLowerCase() + "%";
                predicates.add(
                        criteriaBuilder.or(
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("cardNumber")), searchTerm),
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("cardHolder")), searchTerm)
                        )
                );
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Card findUserCard(Long userId, String cardNumber) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        //чтобы вводить последние 4 цифры
        String formatCardNumber = "**** **** **** " + cardNumber;

        return cardRepository.findByCardNumberAndUser_UserId(formatCardNumber, userId)
                .orElseThrow(() -> new CardNotFoundException(formatCardNumber, user.getEmail()));
    }

    private void validateCardBlock(Card card) {
        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new CardOperationException("Card is already blocked");
        }
    }
}
