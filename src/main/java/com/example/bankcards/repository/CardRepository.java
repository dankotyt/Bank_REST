package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long>, JpaSpecificationExecutor<Card> {
    Optional<Card> findByCardNumber(String cardNumber);
    List<Card> findByUser(User user);
    Optional<Card> findByCardNumberAndUser(String cardNumber, User user);
    Optional<Card> findByCardNumberAndUser_UserId(String cardNumber, Long userId);

    Page<Card> findAll(Specification<Card> spec, Pageable pageable);

//    @Query("SELECT c.cardNumberHash FROM Card c WHERE c.cardNumberHash IN :hashes")
//    List<String> findExistingHashes(@Param("hashes")Collection<String> hashes);
}
