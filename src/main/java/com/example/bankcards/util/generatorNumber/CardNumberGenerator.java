package com.example.bankcards.util.generatorNumber;

import java.util.List;

public interface CardNumberGenerator {
    List<String> generateBatch(int  batchSize);
    String generateNumber();
}
