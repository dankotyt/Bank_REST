package com.example.bankcards.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CardNumberGeneratorTest {

    @InjectMocks
    private CardNumberGenerator cardNumberGenerator;

    @Test
    void generateBatch_ShouldReturnCorrectSize() {
        List<String> result = cardNumberGenerator.generateBatch();

        assertEquals(100, result.size());
    }

    @Test
    void generateBatch_ShouldReturnUniqueNumbers() {
        List<String> result = cardNumberGenerator.generateBatch();

        assertEquals(100, result.stream().distinct().count());
    }

    @Test
    void generateNumber_ShouldStartWithBin() {
        String result = cardNumberGenerator.generateNumber();
        String lastFourDigits = result.substring(result.length() - 4);

        assertTrue(result.startsWith("**** **** **** " + lastFourDigits));
    }

    @Test
    void generateNumber_ShouldHaveCorrectLength() {
        String result = cardNumberGenerator.generateNumber();

        assertEquals(19, result.length());
    }

    @Test
    void generateNumber_ShouldMatchMaskPattern() {
        Pattern pattern = Pattern.compile("^\\*{4} \\*{4} \\*{4} \\d{4}$");

        String result = cardNumberGenerator.generateNumber();

        assertTrue(pattern.matcher(result).matches());
        assertEquals(19, result.length());
    }

    @Test
    void maskCardNumber_ShouldMaskCorrectly() {
        String fullNumber = "2202201234567890";

        String result = cardNumberGenerator.maskCardNumber(fullNumber);

        assertEquals("**** **** **** 7890", result);
    }

    @Test
    void maskCardNumber_WithInvalidLength_ShouldReturnNull() {
        String invalidNumber = "123456789";

        String result = cardNumberGenerator.maskCardNumber(invalidNumber);

        assertNull(result);
    }

    @Test
    void generateNumber_ShouldProduceValidLuhnNumber() {
        String maskedNumber = cardNumberGenerator.generateNumber();

        String visiblePart = maskedNumber.substring(maskedNumber.length() - 4);

        assertEquals(4, visiblePart.length());
        assertTrue(visiblePart.matches("\\d{4}"));
    }
}