package com.example.bankcards;

import com.example.bankcards.config.JwtConfig;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtConfig.class)
public class BankCards {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().load();

        System.out.println("JWT_SECRET: " + dotenv.get("JWT_SECRET"));
        System.out.println("JWT_ACCESS_TTL: " + dotenv.get("JWT_ACCESS_TTL"));
        System.out.println("JWT_REFRESH_TTL: " + dotenv.get("JWT_REFRESH_TTL"));
        System.out.println("USER_LOGIN: " + dotenv.get("USER_LOGIN"));
        System.out.println("USER_PASSWORD: " + dotenv.get("USER_PASSWORD"));
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

        SpringApplication.run(BankCards.class, args);
    }
}
