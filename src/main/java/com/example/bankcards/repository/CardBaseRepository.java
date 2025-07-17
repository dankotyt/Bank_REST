package com.example.bankcards.repository;

import com.example.bankcards.entity.CardBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CardBaseRepository extends JpaRepository<CardBase, Long> {
    Optional<CardBase> findByCardNumber(String cardNumber);
    @Query(value = "SELECT * FROM card_base ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<String> findRandomCard();
    void deleteByCardNumber(String cardNumber);
}
