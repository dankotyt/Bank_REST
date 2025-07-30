package com.example.bankcards.util.generatorNumber;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class CardNumberGeneratorImpl implements CardNumberGenerator {
    private static final String BIN = "220220";
    //private final EncryptionCard encryptionCard;

    @Override
    public List<String> generateBatch(int batchSize) {
        List<String> cardNumbers = new ArrayList<>();
        while (cardNumbers.size() < batchSize) {
            String number = generateNumber();
            cardNumbers.add(number);
        }
        return cardNumbers;

//        List<String> existingHashes = cardRepository.findExistingHashes(hashes);
//        existingHashes.forEach(hashes::remove);
//
//        return cardNumbers.stream()
//                .filter(n -> hashes.contains(encryptionCard.encrypt(n)))
//                .limit(batchSize)
//                .collect(Collectors.toList());
    }

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

    String maskCardNumber(String cardNumber) {
        if (cardNumber.length() == 16) {
            return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
        }
        return null;
    }
}
