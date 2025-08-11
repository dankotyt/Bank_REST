package com.example.bankcards.util.generatorNumbers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Реализация генератора номеров банковских карт.
 * <p>
 * Генерирует 16-значные номера карт, начиная с BIN "220220",
 * с валидной контрольной цифрой по алгоритму Луна.
 */
@Component
@RequiredArgsConstructor
public class CardNumberGeneratorImpl implements CardNumberGenerator {
    /** BIN (Bank Identification Number) для генерации карт */
    private static final String BIN = "220220";

    /**
     * Генерирует номер банковской карты.
     * <p>
     * Генерация включает:
     * - 6 цифр BIN
     * - 9 случайных цифр
     * - 1 контрольную цифру (алгоритм Луна)
     * - Маскировку первых 12 цифр
     *
     * @return Замаскированный номер карты в формате "**** **** **** XXXX"
     * @see CardNumberGenerator#generateNumber()
     */
    @Override
    public String generateNumber() {
        Random random = ThreadLocalRandom.current();
        StringBuilder number = new StringBuilder(BIN);

        for (int i = 0; i < 9; i++) {
            number.append(random.nextInt(10));
        }

        String partialNumber = number.toString();
        int checkDigit = checkDigit(partialNumber);
        return maskCardNumber(partialNumber + checkDigit);
    }

    /**
     * Вычисляет контрольную цифру номера карты по алгоритму Луна.
     *
     * @param number Частичный номер карты (без контрольной цифры)
     * @return Контрольная цифра (0-9)
     * @throws NumberFormatException если строка содержит нечисловые символы
     */
    int checkDigit(String number) {
        int sum = 0;
        boolean checker = false;

        for (int i = number.length() - 1; i >= 0 ; i--) {
            int digit = Character.getNumericValue(number.charAt(i));
            if (checker) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }
            sum += digit;
            checker = !checker;
        }
        return (10 - (sum % 10)) % 10;
    }

    /**
     * Маскирует номер карты, оставляя видимыми только последние 4 цифры.
     *
     * @param cardNumber Полный номер карты (16 цифр)
     * @return Замаскированный номер в формате "**** **** **** XXXX"
     *         или null, если входная строка не соответствует ожидаемой длине
     */
    String maskCardNumber(String cardNumber) {
        if (cardNumber.length() == 16) {
            return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
        }
        return null;
    }
}

