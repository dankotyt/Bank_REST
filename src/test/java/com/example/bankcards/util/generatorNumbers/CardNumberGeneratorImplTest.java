package com.example.bankcards.util.generatorNumbers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CardNumberGeneratorImplTest {

    @InjectMocks
    private CardNumberGeneratorImpl cardNumberGeneratorImpl;

    @Test
    void generateNumber_ShouldStartWithBin() {
        String result = cardNumberGeneratorImpl.generateNumber();
        String lastFourDigits = result.substring(result.length() - 4);

        assertTrue(result.startsWith("**** **** **** " + lastFourDigits));
    }

    @Test
    void generateNumber_ShouldHaveCorrectLength() {
        String result = cardNumberGeneratorImpl.generateNumber();

        assertEquals(19, result.length());
    }

    @Test
    void generateNumber_ShouldMatchMaskPattern() {
        Pattern pattern = Pattern.compile("^\\*{4} \\*{4} \\*{4} \\d{4}$");

        String result = cardNumberGeneratorImpl.generateNumber();

        assertTrue(pattern.matcher(result).matches());
        assertEquals(19, result.length());
    }

    @Test
    void maskCardNumber_ShouldMaskCorrectly() {
        String fullNumber = "2202201234567890";

        String result = cardNumberGeneratorImpl.maskCardNumber(fullNumber);

        assertEquals("**** **** **** 7890", result);
    }

    @Test
    void maskCardNumber_WithInvalidLength_ShouldReturnNull() {
        String invalidNumber = "123456789";

        String result = cardNumberGeneratorImpl.maskCardNumber(invalidNumber);

        assertNull(result);
    }

    @Test
    void generateNumber_ShouldProduceValidLuhnNumber() {
        String maskedNumber = cardNumberGeneratorImpl.generateNumber();

        String visiblePart = maskedNumber.substring(maskedNumber.length() - 4);

        assertEquals(4, visiblePart.length());
        assertTrue(visiblePart.matches("\\d{4}"));
    }
}