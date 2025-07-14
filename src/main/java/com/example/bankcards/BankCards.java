package com.example.bankcards;

import com.example.bankcards.config.JwtConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtConfig.class)
public class BankCards {
    public static void main(String[] args) {
        SpringApplication.run(BankCards.class, args);
    }
}
